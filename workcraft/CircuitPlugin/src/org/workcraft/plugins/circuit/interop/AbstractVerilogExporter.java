package org.workcraft.plugins.circuit.interop;

import org.workcraft.dom.Model;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.circuit.utils.ArbitrationUtils;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.plugins.circuit.verilog.SubstitutionRule;
import org.workcraft.plugins.circuit.verilog.SubstitutionUtils;
import org.workcraft.plugins.circuit.verilog.VerilogBus;
import org.workcraft.plugins.circuit.verilog.VerilogWriter;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Wait;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;

import java.io.File;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractVerilogExporter implements Exporter {

    private final Queue<Pair<File, Circuit>> refinementCircuits = new ArrayDeque<>();

    @Override
    public abstract VerilogFormat getFormat();

    @Override
    public boolean isCompatible(Model model) {
        return model instanceof Circuit;
    }

    @Override
    public void serialise(Model model, OutputStream out) {
        if (model instanceof Circuit circuit) {
            VerilogWriter writer = new VerilogWriter(out);
            String moduleName = ExportUtils.getTitleAsIdentifier(circuit.getTitle());
            File file = getCurrentFile();
            writer.write(ExportUtils.getExportHeader("Verilog netlist", "//", moduleName, file, getFormat()));
            refinementCircuits.clear();
            if (!getFormat().useSystemVerilogSyntax()) {
                writer.writeTimescaleDefinition();
            }
            writeCircuit(writer, circuit, moduleName);
            writeRefinementCircuits(writer);
            writer.close();
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
    }

    private void writeCircuit(VerilogWriter writer, Circuit circuit, String moduleName) {
        Set<FunctionComponent> badComponents = RefinementUtils.getIncompatibleRefinementCircuitComponents(circuit);
        if (!badComponents.isEmpty()) {
            LogUtils.logError(TextUtils.wrapMessageWithItems(
                    "Verilog module '" + moduleName + "' uses incompatible refinement for component",
                    ReferenceHelper.getReferenceSet(circuit, badComponents)));
        }
        CircuitSignalInfo circuitInfo = new CircuitSignalInfo(circuit);
        writeHeader(writer, circuitInfo, moduleName);
        writeInstances(writer, circuitInfo);
        writeInitialState(writer, circuitInfo);
        writer.writeModuleOutro();
    }

    private void writeRefinementCircuits(VerilogWriter writer) {
        Map<String, String> exportedModules = new HashMap<>();
        while (!refinementCircuits.isEmpty()) {
            Pair<File, Circuit> refinement = refinementCircuits.remove();
            Circuit circuit = refinement.getSecond();
            String moduleName = ExportUtils.getTitleAsIdentifier(circuit.getTitle());
            String path = FileUtils.getFullPath(refinement.getFirst());
            if (!exportedModules.containsKey(moduleName)) {
                exportedModules.put(moduleName, path);
                writer.write('\n');
                writeCircuit(writer, circuit, moduleName);
            } else {
                String exportedPath = exportedModules.get(moduleName);
                if ((path != null) && !path.equals(exportedPath)) {
                    LogUtils.logError(
                            "Different circuit refinement has been used for Verilog module '" + moduleName + "'"
                                    + TextUtils.getBulletpoint("Original refinement: " + exportedPath)
                                    + TextUtils.getBulletpoint("Conflict refinement: " + path));
                }
            }
        }
    }

    private void writeHeader(VerilogWriter writer, CircuitSignalInfo circuitInfo, String moduleName) {
        Set<String> inputPorts = new LinkedHashSet<>();
        Set<String> outputPorts = new LinkedHashSet<>();
        Circuit circuit = circuitInfo.getCircuit();
        for (Contact contact : circuit.getPorts()) {
            String signal = circuitInfo.getContactSignal(contact);
            Set<String> ports = contact.isInput() ? inputPorts : outputPorts;
            ports.add(signal);
            if (circuitInfo.getBusIndexes(signal) != null) {
                circuitInfo.removeBus(signal);
                LogUtils.logWarning("Nets of bus '" + signal + "' cannot be merged because "
                        + (contact.isInput() ? "input" : "output") + " port with the same name exists");
            }
        }

        Set<String> wires = calcWires(circuitInfo);
        wires.removeAll(inputPorts);
        wires.removeAll(outputPorts);
        adjustBuses(inputPorts, outputPorts, wires, circuitInfo);

        Set<VerilogBus> inputBuses = extractSignalBuses(inputPorts, circuitInfo);
        Set<VerilogBus> outputBuses = extractSignalBuses(outputPorts, circuitInfo);
        writer.writeModuleIntro(moduleName, inputPorts, inputBuses, outputPorts, outputBuses);
        writer.writeSignalDefinitions(VerilogWriter.SignalType.INPUT, inputPorts, inputBuses);
        writer.writeSignalDefinitions(VerilogWriter.SignalType.OUTPUT, outputPorts, outputBuses);
        Set<VerilogBus> wireBuses = extractSignalBuses(wires, circuitInfo);
        writer.writeSignalDefinitions(VerilogWriter.SignalType.WIRE, wires, wireBuses);
        writer.write('\n');
        if (getFormat().useSystemVerilogSyntax()) {
            writer.writeTimeunitDefinition();
        }
    }

    private Set<String> calcWires(CircuitSignalInfo circuitInfo) {
        Set<String> result = new LinkedHashSet<>();
        Circuit circuit = circuitInfo.getCircuit();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (FunctionContact contact : component.getFunctionOutputs()) {
                String signal = circuitInfo.getContactSignal(contact);
                if (signal != null) {
                    result.add(signal);
                }
                if (circuitInfo.getBusIndexes(signal) != null) {
                    circuitInfo.removeBus(signal);
                    LogUtils.logWarning("Nets of bus '" + signal + "' cannot be merged because " +
                            "wire with the same name exists");
                }
            }
        }
        // Auxiliary wires for assign-statement models of WAIT (non-persistent input) and Early MUTEX (for grants)
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (useAssignStatement(component)) {
                if (component.isMutex()) {
                    Set<String> auxMutexWires = getAuxMutexWires(circuitInfo, component);
                    if (!auxMutexWires.isEmpty()) {
                        String componentRef = circuit.getComponentReference(component);
                        String message = "Assign-statement model for Early protocol MUTEX " + componentRef + " uses auxiliary wire";
                        LogUtils.logInfo(TextUtils.wrapMessageWithItems(message, auxMutexWires));
                        result.addAll(auxMutexWires);
                    }
                } else if (component.isWait()) {
                    Set<String> auxWaitWires = getAuxWaitWires(circuitInfo, component);
                    if (!auxWaitWires.isEmpty()) {
                        String componentRef = circuit.getComponentReference(component);
                        String message = "Assign-statement model for WAIT element " + componentRef + " uses auxiliary wire";
                        LogUtils.logInfo(TextUtils.wrapMessageWithItems(message, auxWaitWires));
                        result.addAll(auxWaitWires);
                    }
                }
            }
        }
        return result;
    }

    private boolean useAssignStatement(FunctionComponent component) {
        return (getFormat().useAssignOnly() || !component.isMapped()) && (component.getRefinementFile() == null);
    }

    private static Set<String> getAuxMutexWires(CircuitSignalInfo circuitInfo, FunctionComponent component) {
        Set<String> result = new LinkedHashSet<>();
        Circuit circuit = circuitInfo.getCircuit();
        ArbitrationUtils.MutexData mutexData = ArbitrationUtils.getMutexData(circuit, component, null);
        if ((mutexData != null) && (mutexData.mutexProtocol() == Mutex.Protocol.EARLY)) {
            String g1AuxNetName = circuitInfo.getContactAuxiliarySignal(mutexData.g1OutputPin());
            if (g1AuxNetName != null) {
                result.add(g1AuxNetName);
            }
            String g2AuxNetName = circuitInfo.getContactAuxiliarySignal(mutexData.g2OutputPin());
            if (g2AuxNetName != null) {
                result.add(g2AuxNetName);
            }
        }
        return result;
    }

    private static Set<String> getAuxWaitWires(CircuitSignalInfo circuitInfo, FunctionComponent component) {
        Set<String> result = new LinkedHashSet<>();
        Circuit circuit = circuitInfo.getCircuit();
        ArbitrationUtils.WaitData waitData = ArbitrationUtils.getWaitData(circuit, component, null);
        if (waitData != null) {
            String sigAuxNetName = circuitInfo.getContactAuxiliarySignal(waitData.sigInputPin());
            if (sigAuxNetName != null) {
                result.add(sigAuxNetName);
            }
        }
        return result;
    }

    private void adjustBuses(Set<String> inputs, Set<String> outputs, Set<String> wires,
            CircuitSignalInfo circuitInfo) {

        Set<String> inputBusNames = getBusNames(inputs, circuitInfo);
        Set<String> outputBusNames = getBusNames(outputs, circuitInfo);
        Set<String> wireBusNames = getBusNames(wires, circuitInfo);

        Set<String> overlapBusNames = new HashSet<>();
        overlapBusNames.addAll(SetUtils.intersection(inputBusNames, outputBusNames));
        overlapBusNames.addAll(SetUtils.intersection(outputBusNames, wireBusNames));
        overlapBusNames.addAll(SetUtils.intersection(wireBusNames, inputBusNames));

        for (String busName : overlapBusNames) {
            circuitInfo.removeBus(busName);
            LogUtils.logWarning("Nets of bus '" + busName + "' cannot be merged as they are of different types");
        }
    }

    private Set<String> getBusNames(Set<String> signals, CircuitSignalInfo circuitInfo) {
        Pattern pattern = CircuitSettings.getBusSignalPattern();
        Set<String> result = new HashSet<>();
        for (String signal : signals) {
            Matcher matcher = pattern.matcher(signal);
            if (matcher.matches()) {
                String busName = matcher.group(1);
                if (circuitInfo.getBusIndexes(busName) != null) {
                    result.add(busName);
                }
            }
        }
        return result;
    }

    private void writeInstances(VerilogWriter writer, CircuitSignalInfo circuitInfo) {
        // Write assign statements
        boolean hasAssignments = false;
        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            if (useAssignStatement(component)) {
                if (!getFormat().useAssignOnly()) {
                    String instanceFlatName = circuitInfo.getComponentFlattenReference(component);
                    LogUtils.logWarning("Component " + instanceFlatName
                            + " is not associated to a module and is exported as assign statement.");
                }
                if (writeAssign(writer, circuitInfo, component)) {
                    hasAssignments = true;
                } else {
                    String ref = circuitInfo.getCircuit().getComponentReference(component);
                    LogUtils.logError("Component " + ref + " cannot be exported as assign statement.");
                }
            }
        }
        if (hasAssignments) {
            writer.write('\n');
        }
        // Write mapped components
        boolean hasMappedComponents = false;
        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            if (!useAssignStatement(component)) {
                writeInstance(writer, circuitInfo, component);
                hasMappedComponents = true;
            }
        }
        if (hasMappedComponents) {
            writer.write('\n');
        }
    }

    private static boolean writeAssign(VerilogWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        boolean result = false;
        if (component.isMutex()) {
            result = writeMutexAssign(writer, circuitInfo, component);
        } else if (component.isWait()) {
            result = writeWaitAssign(writer, circuitInfo, component);
        } else {
            result = writeGateAssign(writer, circuitInfo, component);
        }
        return result;
    }

    private static boolean writeMutexAssign(VerilogWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        boolean result = false;
        Circuit circuit = circuitInfo.getCircuit();
        String errorPrefix = "Error writing assign statement for Mutex "
                + circuit.getComponentReference(component) + ": ";

        ArbitrationUtils.MutexData mutexData = ArbitrationUtils.getMutexData(circuit, component, null);
        if (mutexData == null) {
            LogUtils.logWarning(errorPrefix + "cannot match pins or protocol");
        } else {
            String g1NetName = getNetName(mutexData.g1OutputPin(), circuitInfo);
            String g2NetName = getNetName(mutexData.g2OutputPin(), circuitInfo);
            String r1NetName = getNetName(mutexData.r1InputPin(), circuitInfo);
            String r2NetName = getNetName(mutexData.r2InputPin(), circuitInfo);
            if ((g1NetName == null) || (g2NetName == null) || (r1NetName == null) || (r2NetName == null)) {
                LogUtils.logWarning(errorPrefix + "cannot identify nets connected to pins");
            } else {
                if (mutexData.mutexProtocol() == Mutex.Protocol.EARLY) {
                    String g1AuxNetName = circuitInfo.getContactAuxiliarySignal(mutexData.g1OutputPin());
                    String g2AuxNetName = circuitInfo.getContactAuxiliarySignal(mutexData.g2OutputPin());
                    writer.writeAssign(CircuitSettings::getMutexEarlyGrantDelay, g1NetName, g1AuxNetName);
                    writer.writeAssign(CircuitSettings::getMutexEarlyGrantDelay, g2NetName, g2AuxNetName);
                    g1NetName = g1AuxNetName;
                    g2NetName = g2AuxNetName;
                }
                String netName = "{%s, %s}".formatted(g1NetName, g2NetName);
                String expr = getMutexExpression(g1NetName, g2NetName, r1NetName, r2NetName);
                writer.writeAssign(CircuitSettings::getVerilogAssignDelay, netName, expr);
                result = true;
            }
        }
        return result;
    }

    private static String getMutexExpression(String g1, String g2, String r1, String r2) {
        String winner = switch (CircuitSettings.getMutexArbitrationWinner()) {
            case RANDOM -> "$urandom_range(1, 2)";
            case FIRST -> "2'b01";
            case SECOND -> "2'b10";
        };

        return "{%s, %s, %s, %s} == 4'b0011 ? %s : ".formatted(g1, g2, r1, r2, winner)
                + "{%s & (~%s | ~%s), %s & (~%s | ~%s)}".formatted(r1, g2, r2, r2, g1, r1);
    }

    private static boolean writeWaitAssign(VerilogWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        boolean result = false;
        Circuit circuit = circuitInfo.getCircuit();
        String errorPrefix = "Error writing assign statement for Wait "
                + circuit.getComponentReference(component) + ": ";

        ArbitrationUtils.WaitData waitData = ArbitrationUtils.getWaitData(circuit, component, null);
        if (waitData == null) {
            LogUtils.logWarning(errorPrefix + "cannot match pins or type");
        } else {
            String sigNetName = getNetName(waitData.sigInputPin(), circuitInfo);
            String ctrlNetName = getNetName(waitData.ctrlInputPin(), circuitInfo);
            String sanNetName = getNetName(waitData.sanOutputPin(), circuitInfo);
            if ((sigNetName == null) || (ctrlNetName == null) || (sanNetName == null)) {
                LogUtils.logWarning(errorPrefix + "cannot identify nets connected to pins");
            } else {
                String sigTempName = circuitInfo.getContactAuxiliarySignal(waitData.sigInputPin());
                String sigExpr = getWaitSigExpression(sigNetName);
                writer.writeAssign(CircuitSettings::getWaitSigIgnoreTime, sigTempName, sigExpr);

                boolean negateSig = waitData.waitType() == Wait.Type.WAIT0;
                String expr = getWaitExpression(sigTempName, ctrlNetName, sanNetName, negateSig);
                writer.writeAssign(CircuitSettings::getVerilogAssignDelay, sanNetName, expr);
                result = true;
            }
        }
        return result;
    }

    private static String getWaitSigExpression(String sig) {
        String interpretation = switch (CircuitSettings.getWaitUndefinedInterpretation()) {
            case RANDOM -> "$urandom_range(0, 1)";
            case HIGH -> "1'b1";
            case LOW -> "1'b0";
        };

        return "(%s !== 1'b0) && (%s !== 1'b1) ? %s : %s".formatted(sig, sig, interpretation, sig);
    }

    private static String getWaitExpression(String sig, String ctrl, String san, boolean negateSig) {
        return (negateSig ? "%s & (~%s | %s)" : "%s & (%s | %s)").formatted(ctrl, sig, san);
    }

    private static boolean writeGateAssign(VerilogWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        boolean result = false;
        Collection<CircuitSignalInfo.SignalInfo> signalInfos = circuitInfo.getComponentSignalInfos(component,
                signal -> getNetName(signal, circuitInfo));

        String errorPrefix = "Error writing assign statement for component "
                + circuitInfo.getCircuit().getComponentReference(component) + ": ";

        for (CircuitSignalInfo.SignalInfo signalInfo : signalInfos) {
            String netName = getNetName(signalInfo.contact, circuitInfo);
            if (netName == null) {
                String pinName = signalInfo.contact.getName();
                LogUtils.logWarning(errorPrefix + "cannot identify net connected to pin " + pinName);
                continue;
            }
            String expr = calcGateExpression(signalInfo, netName);
            if (expr == null) {
                LogUtils.logWarning(errorPrefix + "cannot derive expression for net " + netName);
                continue;
            }
            writer.writeAssign(component.getIsZeroDelay() ? null : CircuitSettings::getVerilogAssignDelay, netName, expr);
            result = true;
        }
        return result;
    }

    private static String calcGateExpression(CircuitSignalInfo.SignalInfo signalInfo, String netName) {

        String setExpression = StringGenerator.toString(signalInfo.setFormula, StringGenerator.Style.VERILOG);

        String notResetExpression = StringGenerator.toString(FormulaUtils.invert(signalInfo.resetFormula),
                StringGenerator.Style.VERILOG);

        if (!setExpression.isEmpty() && !notResetExpression.isEmpty()) {
            return setExpression + " | " + netName + " & (" + notResetExpression + ")";
        } else if (!setExpression.isEmpty()) {
            return setExpression;
        } else if (!notResetExpression.isEmpty()) {
            return notResetExpression;
        }
        return null;
    }

    private void writeInstance(VerilogWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        // Module name
        String title = component.getModule();
        File refinementCircuitFile = RefinementUtils.getRefinementCircuitFile(component);
        if (refinementCircuitFile != null) {
            try {
                ModelEntry me = WorkUtils.loadModel(refinementCircuitFile);
                Circuit refinementCircuit = WorkspaceUtils.getAs(me, Circuit.class);
                refinementCircuits.add(Pair.of(refinementCircuitFile, refinementCircuit));
                title = refinementCircuit.getTitle();
            } catch (DeserialisationException ignored) {
            }
        }
        String moduleName = ExportUtils.getTitleAsIdentifier(title);
        // Instance name
        String instanceFlatName = circuitInfo.getComponentFlattenReference(component);
        Map<String, SubstitutionRule> substitutionRules = LibraryManager.getExportSubstitutionRules();
        SubstitutionRule substitutionRule = substitutionRules.get(moduleName);
        String msg = "Processing instance '" + instanceFlatName + "'";
        String circuitTitle = circuitInfo.getCircuit().getTitle();
        if (!circuitTitle.isEmpty()) {
            msg += " in circuit '" + circuitTitle + "'";
        }
        msg += ": ";
        moduleName = SubstitutionUtils.getModuleSubstitutionName(moduleName, substitutionRule, msg);
        Map<String, String> contactToSignalMap = new LinkedHashMap<>();
        for (Contact contact : component.getContacts()) {
            String contactName = SubstitutionUtils.getContactSubstitutionName(contact.getName(), substitutionRule, msg);
            String signalName = circuitInfo.getContactSignal(contact);
            if ((signalName == null) || signalName.isEmpty()) {
                LogUtils.logWarning("In component '" + instanceFlatName + "' contact '" + contactName + "' is disconnected.");
                signalName = "";
            }
            contactToSignalMap.put(contactName, signalName);
        }

        if (component.getIsZeroDelay() && (component.isBuffer() || component.isInverter())) {
            writer.write("    // This inverter should have a short delay\n");
        }
        writer.write("    " + moduleName + ' ' + instanceFlatName + " (");
        writeInstanceContacts(writer, circuitInfo, contactToSignalMap);
        if ((substitutionRule != null) && (substitutionRule.extras != null)) {
            writer.print(substitutionRule.extras);
        }
        writer.write(");\n");
    }

    private void writeInstanceContacts(VerilogWriter writer, CircuitSignalInfo circuitInfo,
            Map<String, String> contactToSignalMap) {

        Map<String, Map<Integer, String>> contactBusToIndexedSignalMap = extractContactBuses(contactToSignalMap);
        boolean first = true;
        for (String contactName : contactToSignalMap.keySet()) {
            if (first) {
                first = false;
            } else {
                writer.print(", ");
            }
            String signalName = contactToSignalMap.getOrDefault(contactName, "");
            String netName = getNetName(signalName, circuitInfo);
            if (netName != null) {
                writer.write("." + contactName + "(" + netName + ")");
            }
        }
        for (String contactBusName : contactBusToIndexedSignalMap.keySet()) {
            if (first) {
                first = false;
            } else {
                writer.write(", ");
            }
            Map<Integer, String> indexedSignals = contactBusToIndexedSignalMap.get(contactBusName);
            Set<Integer> indexes = indexedSignals.keySet();
            Integer maxIndex = Collections.max(indexes);
            Integer minIndex = Collections.min(indexes);
            List<String> netNames = new ArrayList<>();
            for (int index = maxIndex; index >= minIndex; --index) {
                String signalName = indexedSignals.getOrDefault(index, "");
                String netName = getNetName(signalName, circuitInfo);
                if (netName != null) {
                    netNames.add(netName);
                }
            }
            String joinedNetNames = String.join(", ", netNames);
            if (CircuitSettings.getDissolveSingletonBus() && (netNames.size() < 2)) {
                writer.write(" ." + contactBusName + "(" + joinedNetNames + ")");
            } else {
                writer.write("\n        ." + contactBusName + "( {" + joinedNetNames + "} )");
            }
        }
    }

    private void writeInitialState(VerilogWriter writer, CircuitSignalInfo circuitInfo) {
        Circuit circuit = circuitInfo.getCircuit();
        Set<Contact> driversAndPorts = new HashSet<>(circuit.getDrivers());
        driversAndPorts.addAll(circuit.getPorts());

        Map<String, Boolean> signalInitState = new TreeMap<>();
        for (Contact contact : driversAndPorts) {
            String signalName = circuitInfo.getContactSignal(contact);
            if ((signalName != null) && !signalName.isEmpty()) {
                boolean state = CircuitUtils.findInitToOneFromDriver(circuit, contact);
                signalInitState.put(signalName, state);
            }
        }
        if (!signalInitState.isEmpty()) {
            writer.write("    // signal values at the initial state:\n");
            writer.write("    //");
            for (Map.Entry<String, Boolean> entry : signalInitState.entrySet()) {
                Boolean state = entry.getValue();
                String signal = entry.getKey();
                String netName = getNetName(signal, circuitInfo);
                if ((state != null) && (netName != null)) {
                    writer.write((state ? " " : " !") + netName);
                }
            }
            writer.write('\n');
        }
    }

    private Set<VerilogBus> extractSignalBuses(Set<String> signalNames, CircuitSignalInfo circuitInfo) {
        Map<String, Set<Integer>> nameToIndexesMap = new HashMap<>();
        Pattern pattern = CircuitSettings.getBusSignalPattern();
        for (String signalName : new HashSet<>(signalNames)) {
            Matcher matcher = pattern.matcher(signalName);
            if (matcher.matches()) {
                String busName = matcher.group(1);
                if (circuitInfo.getBusIndexes(busName) != null) {
                    Integer netIndex = Integer.valueOf(matcher.group(2));
                    Set<Integer> indexes = nameToIndexesMap.computeIfAbsent(busName, key -> new HashSet<>());
                    indexes.add(netIndex);
                    signalNames.remove(signalName);
                }
            }
        }
        Set<VerilogBus> result = new LinkedHashSet<>();
        for (String name : nameToIndexesMap.keySet()) {
            Set<Integer> indexes = nameToIndexesMap.get(name);
            Integer max = Collections.max(indexes);
            Integer min = Collections.min(indexes);
            if (CircuitSettings.getDissolveSingletonBus() && Objects.equals(max, min)) {
                signalNames.add(name);
            } else {
                result.add(new VerilogBus(name, min, max));
            }
        }
        return result;
    }

    private Map<String, Map<Integer, String>> extractContactBuses(Map<String, String> contactToSignalMap) {
        Map<String, Map<Integer, String>> result = new HashMap<>();
        Pattern pattern = CircuitSettings.getBusSignalPattern();
        Set<String> contactNames = new LinkedHashSet<>(contactToSignalMap.keySet());
        for (String contactName : contactNames) {
            Matcher matcher = pattern.matcher(contactName);
            if (matcher.matches()) {
                String contactBusName = matcher.group(1);
                Integer contactNetIndex = Integer.valueOf(matcher.group(2));
                Map<Integer, String> indexedSignals = result.computeIfAbsent(
                        contactBusName, key -> new HashMap<>());

                String signalName = contactToSignalMap.get(contactName);
                indexedSignals.put(contactNetIndex, signalName);
                contactToSignalMap.remove(contactName);
            }
        }
        return result;
    }

    private static String getNetName(Contact contact, CircuitSignalInfo circuitInfo) {
        return getNetName(circuitInfo.getContactSignal(contact), circuitInfo);
    }

    private static String getNetName(String signal, CircuitSignalInfo circuitInfo) {
        if (signal != null) {
            Pattern pattern = CircuitSettings.getBusSignalPattern();
            Matcher matcher = pattern.matcher(signal);
            if (matcher.matches()) {
                String busName = matcher.group(1);
                Set<Integer> busIndexes = circuitInfo.getBusIndexes(busName);
                if (busIndexes == null) {
                    return signal;
                }
                if ((busIndexes.size() == 1) && CircuitSettings.getDissolveSingletonBus()) {
                    return busName;
                }
                int netIndex = Integer.parseInt(matcher.group(2));
                return busName + "[" + netIndex + "]";
            }
        }
        return signal;
    }

}
