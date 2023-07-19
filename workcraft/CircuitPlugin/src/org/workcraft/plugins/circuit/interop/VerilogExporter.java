package org.workcraft.plugins.circuit.interop;

import org.workcraft.dom.Model;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.Not;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.plugins.circuit.verilog.SubstitutionRule;
import org.workcraft.plugins.circuit.verilog.SubstitutionUtils;
import org.workcraft.plugins.circuit.verilog.VerilogBus;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VerilogExporter implements Exporter {

    private static final String KEYWORD_INPUT = "input";
    private static final String KEYWORD_OUTPUT = "output";
    private static final String KEYWORD_WIRE = "wire";
    private static final String KEYWORD_MODULE = "module";
    private static final String KEYWORD_ENDMODULE = "endmodule";
    private static final String KEYWORD_ASSIGN = "assign";
    private static final String KEYWORD_ASSIGN_DELAY = "#";

    private final Queue<Pair<File, Circuit>> refinementCircuits = new LinkedList<>();

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.getInstance();
    }

    @Override
    public boolean isCompatible(Model model) {
        return model instanceof Circuit;
    }

    @Override
    public void serialise(Model model, OutputStream out) {
        if (model instanceof Circuit) {
            PrintWriter writer = new PrintWriter(out);
            Circuit circuit = (Circuit) model;
            String moduleName = ExportUtils.getTitleAsIdentifier(circuit.getTitle());
            File file = getCurrentFile();
            VerilogFormat format = VerilogFormat.getInstance();
            writer.write(ExportUtils.getExportHeader("Verilog netlist", "//", moduleName, file, format));
            refinementCircuits.clear();
            writeCircuit(writer, circuit, moduleName);
            writeRefinementCircuits(writer);
            writer.close();
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
    }

    private void writeCircuit(PrintWriter writer, Circuit circuit, String moduleName) {
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
        writer.write(KEYWORD_ENDMODULE);
        writer.write('\n');
    }

    private void writeRefinementCircuits(PrintWriter writer) {
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
                                    + '\n' + PropertyHelper.BULLET_PREFIX + "Original refinement: " + exportedPath
                                    + '\n' + PropertyHelper.BULLET_PREFIX + "Conflict refinement: " + path);
                }
            }
        }
    }

    private void writeHeader(PrintWriter writer, CircuitSignalInfo circuitInfo, String moduleName) {
        Set<String> inputPorts = new LinkedHashSet<>();
        Set<String> outputPorts = new LinkedHashSet<>();
        for (Contact contact : circuitInfo.getCircuit().getPorts()) {
            String signal = circuitInfo.getContactSignal(contact);
            Set<String> ports = contact.isInput() ? inputPorts : outputPorts;
            ports.add(signal);
            if (circuitInfo.getBusIndexes(signal) != null) {
                circuitInfo.removeBus(signal);
                LogUtils.logWarning("Nets of bus '" + signal + "' cannot be merged because "
                        + (contact.isInput() ? "input" : "output") + " port with the same name exists");
            }
        }

        Set<String> wires = new LinkedHashSet<>();
        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            for (FunctionContact contact : component.getFunctionOutputs()) {
                String signal = circuitInfo.getContactSignal(contact);
                if ((signal == null) || inputPorts.contains(signal) || outputPorts.contains(signal)) continue;
                wires.add(signal);
                if (circuitInfo.getBusIndexes(signal) != null) {
                    circuitInfo.removeBus(signal);
                    LogUtils.logWarning("Nets of bus '" + signal + "' cannot be merged because " +
                            "wire with the same name exists");
                }
            }
        }

        adjustBuses(inputPorts, outputPorts, wires, circuitInfo);

        Set<VerilogBus> inputBuses = extractSignalBuses(inputPorts, circuitInfo);
        Set<VerilogBus> outputBuses = extractSignalBuses(outputPorts, circuitInfo);
        writePortDeclarations(writer, moduleName, inputPorts, inputBuses, outputPorts, outputBuses);

        writeSignalDefinitions(writer, KEYWORD_INPUT, inputPorts, inputBuses);
        writeSignalDefinitions(writer, KEYWORD_OUTPUT, outputPorts, outputBuses);
        Set<VerilogBus> wireBuses = extractSignalBuses(wires, circuitInfo);
        writeSignalDefinitions(writer, KEYWORD_WIRE, wires, wireBuses);
        writer.write('\n');
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

    private void writePortDeclarations(PrintWriter writer, String title,
            Set<String> inputPorts, Set<VerilogBus> inputBuses,
            Set<String> outputPorts, Set<VerilogBus> outputBuses) {

        Set<String> ports = new LinkedHashSet<>();
        ports.addAll(inputPorts);
        ports.addAll(inputBuses.stream().map(VerilogBus::getName).collect(Collectors.toList()));
        ports.addAll(outputPorts);
        ports.addAll(outputBuses.stream().map(VerilogBus::getName).collect(Collectors.toList()));

        writer.write(KEYWORD_MODULE + ' ' + title + " (");
        boolean isFirstPort = true;
        for (String port : ports) {
            if (isFirstPort) {
                isFirstPort = false;
            } else {
                writer.write(", ");
            }
            writer.write(port);
        }
        writer.write(");\n");
    }

    private void writeSignalDefinitions(PrintWriter writer, String keyword, Set<String> signals, Set<VerilogBus> buses) {
        if (!signals.isEmpty()) {
            writer.write("    " + keyword + ' ' + String.join(", ", signals) + ";\n");
        }
        for (VerilogBus bus : buses) {
            Integer maxIndex = bus.getMaxIndex();
            Integer minIndex = bus.getMinIndex();
            String name = bus.getName();
            writer.write("    " + keyword + " [" + maxIndex + ':' + minIndex + "] " + name + ";\n");
        }
    }

    private void writeInstances(PrintWriter writer, CircuitSignalInfo circuitInfo) {
        // Write writer assign statements
        boolean hasAssignments = false;
        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            if (!component.isMapped() && (component.getRefinementFile() == null)) {
                if (writeAssigns(writer, circuitInfo, component)) {
                    hasAssignments = true;
                } else {
                    String ref = circuitInfo.getCircuit().getComponentReference(component);
                    LogUtils.logError("Unmapped component '" + ref + "' cannot be exported as assign statements.");
                }
            }
        }
        if (hasAssignments) {
            writer.write('\n');
        }
        // Write writer mapped components
        boolean hasMappedComponents = false;
        for (FunctionComponent component : circuitInfo.getCircuit().getFunctionComponents()) {
            if (component.isMapped() || (component.getRefinementFile() != null)) {
                writeInstance(writer, circuitInfo, component);
                hasMappedComponents = true;
            }
        }
        if (hasMappedComponents) {
            writer.write('\n');
        }
    }

    private boolean writeAssigns(PrintWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
        boolean result = false;
        String instanceFlatName = circuitInfo.getComponentFlattenReference(component);
        LogUtils.logWarning("Component '" + instanceFlatName + "' is not associated to a module and is exported as assign statement.");
        for (CircuitSignalInfo.SignalInfo signalInfo : circuitInfo.getComponentSignalInfos(component)) {
            String signalName = circuitInfo.getContactSignal(signalInfo.contact);
            BooleanFormula setFormula = signalInfo.setFormula;
            String setExpr = StringGenerator.toString(setFormula, StringGenerator.Style.VERILOG);
            BooleanFormula resetFormula = signalInfo.resetFormula;
            if (resetFormula != null) {
                resetFormula = new Not(resetFormula);
            }
            String resetExpr = StringGenerator.toString(resetFormula, StringGenerator.Style.VERILOG);
            String expr = null;
            if (!setExpr.isEmpty() && !resetExpr.isEmpty()) {
                expr = setExpr + " | " + signalName + " & (" + resetExpr + ")";
            } else if (!setExpr.isEmpty()) {
                expr = setExpr;
            } else if (!resetExpr.isEmpty()) {
                expr = resetExpr;
            }
            if (expr != null) {
                writer.write("    " + KEYWORD_ASSIGN + getDelayParameter() + ' ' + signalName + " = " + expr + ";\n");
                result = true;
            }
        }
        return result;
    }

    private String getDelayParameter() {
        String assignDelay = CircuitSettings.getVerilogAssignDelay();
        return (assignDelay == null) || assignDelay.isEmpty() || "0".equals(assignDelay.trim())
                ? "" : ' ' + KEYWORD_ASSIGN_DELAY + assignDelay;
    }

    private void writeInstance(PrintWriter writer, CircuitSignalInfo circuitInfo, FunctionComponent component) {
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

    private void writeInstanceContacts(PrintWriter writer, CircuitSignalInfo circuitInfo,
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
            writer.write("." + contactName + "(" + netName + ")");
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
                netNames.add(getNetName(signalName, circuitInfo));
            }
            String joinedNetNames = String.join(", ", netNames);
            if (CircuitSettings.getDissolveSingletonBus() && (netNames.size() < 2)) {
                writer.write(" ." + contactBusName + "(" + joinedNetNames + ")");
            } else {
                writer.write("\n        ." + contactBusName + "( {" + joinedNetNames + "} )");
            }
        }
    }

    private void writeInitialState(PrintWriter writer, CircuitSignalInfo circuitInfo) {
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
                writer.write((state ? " " : " !") + netName);
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

    private String getNetName(String signal, CircuitSignalInfo circuitInfo) {
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
        return signal;
    }

}
