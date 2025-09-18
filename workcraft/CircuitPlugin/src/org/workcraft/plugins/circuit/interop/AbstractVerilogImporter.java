package org.workcraft.plugins.circuit.interop;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.*;
import org.workcraft.formula.*;
import org.workcraft.formula.bdd.BddManager;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.expression.Expression;
import org.workcraft.plugins.circuit.expression.Literal;
import org.workcraft.plugins.circuit.genlib.*;
import org.workcraft.plugins.circuit.jj.expression.ExpressionParser;
import org.workcraft.plugins.circuit.utils.*;
import org.workcraft.plugins.circuit.verilog.*;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Wait;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractVerilogImporter implements Importer {

    private static final String TITLE = "Import of Verilog netlist";

    private final boolean celementAssign;
    private final boolean sequentialAssign;

    private Map<VerilogModule, String> moduleToFileNameMap = null;
    private boolean isFlatNetlist = false;

    /**
     * @param connections (portName -> netName)
     */
    private record AssignGate(
            String outputName,
            String setFunction,
            String resetFunction,
            HashMap<String, String> connections) { }

    private static final class Net {
        public final Set<FunctionContact> sources = new HashSet<>();
        public final Set<FunctionContact> sinks = new HashSet<>();
        public final Set<FunctionContact> undefined = new HashSet<>();
    }

    public AbstractVerilogImporter(boolean celementAssign, boolean sequentialAssign) {
        this.celementAssign = celementAssign;
        this.sequentialAssign = sequentialAssign;
    }

    @Override
    public ModelEntry deserialise(InputStream in, String serialisedUserData)
            throws OperationCancelledException, DeserialisationException {

        moduleToFileNameMap = null;
        Collection<VerilogModule> verilogModules = VerilogUtils.importVerilogModules(in);
        if ((verilogModules == null) || verilogModules.isEmpty()) {
            throw new DeserialisationException("No Verilog modules could be imported");
        }

        Set<String> undefinedModules = VerilogUtils.getUndefinedModules(verilogModules);
        if (!undefinedModules.isEmpty()) {
            String prefix = TextUtils.wrapMessageWithItems("Verilog netlist refers to undefined module", undefinedModules);
            String message = TextUtils.getHeadAndTail(prefix, 10, 0)
                    + "\n\nInstances of these modules may be interpreted incorrectly due to missing information about input/output pins."
                    + "\n\nPossible causes of the problem:"
                    + TextUtils.getBulletpoint("incorrect path to gate library GenLib file")
                    + TextUtils.getBulletpoint("incorrect setup for MUTEX and WAIT elements")
                    + TextUtils.getBulletpoint("missing module definition in hierarchical Verilog netlist")
                    + TextUtils.getBulletpoint("incorrect use of substitution rules for Verilog import");

            String question = "\n\nProceed with Verilog import anyway?";
            // Choose "Yes" as default response in no-GUI mode
            boolean defaultChoice = !Framework.getInstance().isInGuiMode();
            if (!DialogUtils.showConfirmWarning(message, question, TITLE, defaultChoice)) {
                throw new OperationCancelledException();
            }
        }

        String topModuleName = extractTopModuleName(serialisedUserData);
        VerilogModule topVerilogModule = topModuleName == null ? VerilogUtils.getTopModule(verilogModules)
                : verilogModules.stream().filter(m -> topModuleName.equals(m.name)).findAny().orElse(null);

        if (topVerilogModule == null) {
            String message = "Cannot find suggested top module '" + topModuleName + "'";
            String question = ".\n\nProceed with Verilog import anyway?";
            // Do not proceed in no-GUI mode if the suggested top module is incorrect
            if (!DialogUtils.showConfirmWarning(message, question, TITLE, false)) {
                throw new OperationCancelledException();
            }
            topVerilogModule = VerilogUtils.getTopModule(verilogModules);
        }

        isFlatNetlist = (verilogModules.size() == 1);
        return isFlatNetlist ? createCircuitModelEntry(topVerilogModule)
                : createCircuitModelEntryWithHierarchy(verilogModules, topVerilogModule);
    }

    private String extractTopModuleName(String serialisedUserData) {
        return serialisedUserData;
    }

    private ModelEntry createCircuitModelEntry(VerilogModule verilogModule) throws DeserialisationException {
        Circuit circuit = createCircuit(verilogModule, Collections.emptySet());
        return new ModelEntry(new CircuitDescriptor(), circuit);
    }

    private ModelEntry createCircuitModelEntryWithHierarchy(Collection<VerilogModule> verilogModules,
            VerilogModule topVerilogModule) throws DeserialisationException, OperationCancelledException {

        if (Framework.getInstance().isInGuiMode()) {
            return createCircuitModelEntryWithHierarchyGui(verilogModules, topVerilogModule);
        } else {
            return createCircuitModelEntryWithHierarchyAuto(verilogModules, topVerilogModule);
        }
    }

    private ModelEntry createCircuitModelEntryWithHierarchyGui(Collection<VerilogModule> verilogModules,
            VerilogModule topVerilogModule) throws OperationCancelledException, DeserialisationException {

        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        ImportVerilogDialog dialog = new ImportVerilogDialog(mainWindow, Pair.of(verilogModules, topVerilogModule));
        if (!dialog.reveal()) {
            throw new OperationCancelledException();
        }
        topVerilogModule = dialog.getTopModule();
        File dir = dialog.getDirectory();
        Set<VerilogModule> instantiatedModules = VerilogUtils.getInstantiatedModules(topVerilogModule, verilogModules);
        moduleToFileNameMap = dialog.getModuleToFileNameMap();
        Collection<String> existingSaveFilePaths = getExistingSaveFilePaths(instantiatedModules, dir);
        if (!existingSaveFilePaths.isEmpty()) {
            int existingSaveFileCount = existingSaveFilePaths.size();
            StringBuilder message = new StringBuilder(existingSaveFileCount > 1
                    ? "The following " + existingSaveFileCount + " files already exist:"
                    : "The following file already exists:");

            for (String existingSaveFilePath : existingSaveFilePaths) {
                message.append(TextUtils.getBulletpoint(existingSaveFilePath));
            }
            LogUtils.logWarning(message.toString());

            String question = existingSaveFileCount == 1
                    ? "\n\nOverwrite these files and proceed with import?"
                    : "\n\nOverwrite this file and proceed with import?";

            String shortMessage = TextUtils.getHeadAndTail(message.toString(), 10, 0);
            if (!DialogUtils.showConfirm(shortMessage, question, TITLE,
                    false, JOptionPane.WARNING_MESSAGE, false)) {

                throw new OperationCancelledException();
            }
        }
        Circuit topCircuit = createCircuitHierarchy(topVerilogModule, instantiatedModules, dir);
        ModelEntry me = new ModelEntry(new CircuitDescriptor(), topCircuit);
        if (moduleToFileNameMap != null) {
            me.setDesiredName(moduleToFileNameMap.get(topVerilogModule));
        }
        return me;
    }

    private ModelEntry createCircuitModelEntryWithHierarchyAuto(Collection<VerilogModule> verilogModules,
            VerilogModule topVerilogModule) throws DeserialisationException {

        // Set directory to save work files for instantiated modules
        Framework framework = Framework.getInstance();
        File dir = framework.getImportContextDirectory();
        if (dir == null) {
            dir = framework.getWorkingDirectory();
        }
        Set<VerilogModule> instantiatedModules = VerilogUtils.getInstantiatedModules(topVerilogModule, verilogModules);
        moduleToFileNameMap = VerilogUtils.getModuleToFileMap(verilogModules);
        Collection<String> existingSaveFilePaths = getExistingSaveFilePaths(instantiatedModules, dir);
        if (!existingSaveFilePaths.isEmpty()) {
            String msg = "Import of circuit hierarchy overwrites the following files:\n"
                    + String.join("\n", existingSaveFilePaths);

            LogUtils.logWarning(msg);
        }
        Circuit topCircuit = createCircuitHierarchy(topVerilogModule, instantiatedModules, dir);
        ModelEntry me = new ModelEntry(new CircuitDescriptor(), topCircuit);
        if (moduleToFileNameMap != null) {
            me.setDesiredName(moduleToFileNameMap.get(topVerilogModule));
        }
        return me;
    }

    private Collection<String> getExistingSaveFilePaths(Set<VerilogModule> instantiatedModules, File dir) {
        Collection<String> result = new HashSet<>();
        for (VerilogModule module : instantiatedModules) {
            String fileName = moduleToFileNameMap.get(module);
            File file = new File(dir, fileName);
            if (file.exists()) {
                result.add(file.getAbsolutePath());
            }
        }
        return result;
    }

    private Circuit createCircuitHierarchy(VerilogModule topVerilogModule, Collection<VerilogModule> instantiatedModules,
            File dir) throws DeserialisationException {

        Circuit circuit = createCircuit(topVerilogModule, Collections.emptySet(), instantiatedModules);

        Map<Circuit, String> circuitFileNames = new HashMap<>();
        for (VerilogModule verilogModule : instantiatedModules) {
            Circuit instantiatedCircuit = createCircuit(verilogModule, Collections.emptySet(), instantiatedModules);
            if (moduleToFileNameMap != null) {
                circuitFileNames.put(instantiatedCircuit, moduleToFileNameMap.get(verilogModule));
            }
        }
        adjustModuleRefinements(circuit, circuitFileNames, dir);
        return circuit;
    }

    private void adjustModuleRefinements(Circuit topCircuit, Map<Circuit, String> circuitFileNames, File dir) {
        Framework framework = Framework.getInstance();
        for (Circuit circuit : circuitFileNames.keySet()) {
            ModelEntry me = new ModelEntry(new CircuitDescriptor(), circuit);
            WorkspaceEntry we = framework.createWork(me, circuit.getTitle());
            try {
                String fileName = circuitFileNames.get(circuit);
                File file = FileUtils.getFileByAbsoluteOrRelativePath(fileName, dir);
                framework.saveWork(we, file);
            } catch (SerialisationException e) {
                e.printStackTrace();
            }
        }
        String base = FileUtils.getFullPath(dir);
        for (FunctionComponent component : topCircuit.getFunctionComponents()) {
            FileReference refinement = component.getRefinement();
            if (refinement != null) {
                File file = FileUtils.getFileByPathAndBase(refinement.getPath(), base);
                String path = FileUtils.getFullPath(file);
                refinement.setPath(path);
            }
        }
    }

    public Circuit createCircuit(VerilogModule verilogModule)
            throws DeserialisationException {

        return createCircuit(verilogModule, Collections.emptySet());
    }

    public Circuit createCircuit(VerilogModule verilogModule, Collection<Mutex> mutexes)
            throws DeserialisationException {

        return createCircuit(verilogModule, mutexes, Collections.emptySet());
    }

    private Circuit createCircuit(VerilogModule verilogModule, Collection<Mutex> mutexes,
            Collection<VerilogModule> instantiatedModules) throws DeserialisationException {

        Circuit circuit = new Circuit();
        circuit.setTitle(verilogModule.name);
        HashMap<VerilogInstance, FunctionComponent> instanceComponentMap = new HashMap<>();
        HashMap<String, Net> signalToNetMap = createPorts(circuit, verilogModule, mutexes);
        for (VerilogAssign verilogAssign : verilogModule.assigns) {
            createAssignGate(circuit, verilogAssign, signalToNetMap);
        }
        for (VerilogInstance verilogInstance : verilogModule.instances) {
            SubstitutionRule substitutionRule = null;
            String moduleName = verilogInstance.moduleName;
            Gate gate = createPrimitiveGate(verilogInstance);
            if (gate == null) {
                Library library = LibraryManager.getLibrary();
                if (library != null) {
                    Map<String, SubstitutionRule> substitutionRules = LibraryManager.getImportSubstitutionRules();
                    substitutionRule = substitutionRules.get(moduleName);
                    String msg = "Processing instance '" + verilogInstance.name + "' in module '" + verilogModule.name + "': ";
                    String gateName = SubstitutionUtils.getModuleSubstitutionName(moduleName, substitutionRule, msg);
                    gate = library.get(gateName);
                }
            }

            FunctionComponent component;
            if (gate != null) {
                component = createLibraryGate(circuit, verilogInstance, signalToNetMap, gate, substitutionRule);
            } else if (VerilogUtils.isWaitModule(moduleName)) {
                Wait waitInstance = instanceToWaitWithType(verilogInstance);
                component = createWait(circuit, waitInstance, signalToNetMap);
            } else if (VerilogUtils.isMutexModule(moduleName)) {
                Mutex mutexInstance = instanceToMutexWithProtocol(verilogInstance, mutexes);
                component = createMutex(circuit, mutexInstance, signalToNetMap);
            } else {
                component = createModuleInstance(circuit, verilogInstance, signalToNetMap, instantiatedModules);
            }
            if (component != null) {
                instanceComponentMap.put(verilogInstance, component);
            }
        }
        insertMutexes(mutexes, circuit, signalToNetMap);
        createConnections(circuit, signalToNetMap);
        Map<String, Boolean> signalStates = getSignalState(verilogModule.initialState);
        setInitialStateForPragmas(signalToNetMap, signalStates);
        Set<String> constSignals = setInitialStateForConstants(circuit);
        setZeroDelayAttribute(instanceComponentMap);
        checkImportResult(circuit, signalToNetMap, signalStates.keySet(), constSignals);
        return circuit;
    }

    private Map<String, Boolean> getSignalState(Map<VerilogNet, Boolean> netStates) {
        Map<String, Boolean> result = new HashMap<>();
        if (netStates != null) {
            for (Map.Entry<VerilogNet, Boolean> entry : netStates.entrySet()) {
                VerilogNet verilogNet = entry.getKey();
                Boolean state = entry.getValue();
                String netName = VerilogUtils.getNetBusSuffixName(verilogNet);
                result.put(netName, state);
            }
        }
        return result;
    }

    private void checkImportResult(Circuit circuit, Map<String, Net> signalToNetMap,
            Set<String> initialisedSignals, Set<String> constSignals) {

        String longMessage = "";
        String shortMessage = "";
        // Check circuit for no components
        if (circuit.getFunctionComponents().isEmpty()) {
            String itemText = TextUtils.getBulletpoint("No components");
            longMessage += itemText;
            shortMessage += itemText;
        }
        // Check circuit for hanging contacts
        Set<String> hangingSignals = VerificationUtils.getHangingSignals(circuit);
        if (!hangingSignals.isEmpty()) {
            String itemText = TextUtils.getBulletpoint(TextUtils.wrapMessageWithItems(
                    "Hanging contact", SortUtils.getSortedNatural(hangingSignals)));

            longMessage += itemText;
            shortMessage += TextUtils.getHeadAndTail(itemText, 5, 0);
        }

        // Partition net signals into used and ignored (inout ports)
        Set<String> usedSignals = new HashSet<>();
        Set<String> ignoredSignals = new HashSet<>();
        signalToNetMap.forEach((name, net) -> {
            if (net == null) {
                ignoredSignals.add(name);
            } else {
                usedSignals.add(name);
            }
        });

        // Check circuit for uninitialised signals
        Set<String> uninitialisedSignals = new HashSet<>(usedSignals);
        uninitialisedSignals.addAll(VerificationUtils.getHangingDriverSignals(circuit));
        uninitialisedSignals.removeAll(initialisedSignals);
        uninitialisedSignals.removeAll(constSignals);

        if (!uninitialisedSignals.isEmpty()) {
            String itemText = TextUtils.getBulletpoint(TextUtils.wrapMessageWithItems(
                    "Missing initial state declaration (assuming low) for signal",
                    SortUtils.getSortedNatural(uninitialisedSignals)));

            longMessage += itemText;
            shortMessage += TextUtils.getHeadAndTail(itemText, 5, 0);
        }

        // Check circuit for unused initial state declaration
        Set<String> unusedSignals = new HashSet<>(initialisedSignals);
        unusedSignals.removeAll(usedSignals);
        if (!unusedSignals.isEmpty()) {
            String itemText = TextUtils.getBulletpoint(TextUtils.wrapMessageWithItems(
                    "Initial state declaration for unused signal", SortUtils.getSortedNatural(unusedSignals)));

            longMessage += itemText;
            shortMessage += TextUtils.getHeadAndTail(itemText, 5, 0);
        }

        // Check circuit for ignored signals (inout ports)
        if (!ignoredSignals.isEmpty()) {
            String itemText = TextUtils.getBulletpoint(TextUtils.wrapMessageWithItems(
                    "Ignored inout port", SortUtils.getSortedNatural(ignoredSignals)));

            longMessage += itemText;
            shortMessage += TextUtils.getHeadAndTail(itemText, 5, 0);
        }

        // Produce warning
        if (!longMessage.isEmpty()) {
            String title = circuit.getTitle();
            String intro = "Issues with imported circuit" + (title.isEmpty() ? ":" : (" '" + title + "':"));
            LogUtils.logWarning(intro + longMessage);
            if (isFlatNetlist) {
                DialogUtils.showMessage(intro + shortMessage, TITLE, JOptionPane.WARNING_MESSAGE, false);
            }
        }
    }

    private Wait instanceToWaitWithType(VerilogInstance verilogInstance) {
        Wait module = ArbitrationUtils.getWaitModule(verilogInstance.moduleName);
        if ((module == null) || (module.name == null)) {
            return null;
        }
        String name = verilogInstance.name;
        Signal sig = getPinConnectedSignal(verilogInstance, module.sig.name, 0);
        Signal ctrl = getPinConnectedSignal(verilogInstance, module.ctrl.name, 2);
        Signal san = getPinConnectedSignal(verilogInstance, module.san.name, 3);
        return new Wait(name, module.type, sig, ctrl, san);
    }

    private Mutex instanceToMutexWithProtocol(VerilogInstance verilogInstance, Collection<Mutex> mutexes) {
        String name = verilogInstance.name;
        // Try to derive mutex protocol from STG place with the same name as instance
        Mutex.Protocol mutexProtocol = mutexes.stream()
                .filter(mutex -> (mutex.name != null) && mutex.name.equals(name))
                .findFirst()
                .map(mutex -> mutex.protocol)
                .orElse(null);

        // If there is no corresponding STG mutex place, then try to get it from instance module name
        Mutex mutexModule = (mutexProtocol == null)
                ? ArbitrationUtils.getMutexModule(verilogInstance.moduleName)
                : CircuitSettings.parseMutexData(mutexProtocol);

        // If still cannot determine mutex module, then fall back to Early protocol
        if (mutexModule == null) {
            mutexModule = CircuitSettings.parseMutexData(Mutex.Protocol.EARLY);
        }
        // If no heuristics work, then report error
        if (mutexModule == null) {
            LogUtils.logError(ArbitrationUtils.getMissingMutexMessage(mutexProtocol));
            return null;
        }

        Signal r1 = getPinConnectedSignal(verilogInstance, mutexModule.r1.name, 0);
        Signal g1 = getPinConnectedSignal(verilogInstance, mutexModule.g1.name, 1);
        Signal r2 = getPinConnectedSignal(verilogInstance, mutexModule.r2.name, 2);
        Signal g2 = getPinConnectedSignal(verilogInstance, mutexModule.g2.name, 3);
        return new Mutex(name, r1, g1, r2, g2, mutexModule.protocol);
    }

    private Signal getPinConnectedSignal(VerilogInstance verilogInstance, String portName, int portIndexIfNoPortName) {
        VerilogConnection verilogConnection = null;
        boolean useNamedConnections = true;
        for (VerilogConnection connection : verilogInstance.connections) {
            if (connection.name == null) {
                useNamedConnections = false;
                break;
            } else if (connection.name.equals(portName)) {
                verilogConnection = connection;
                break;
            }
        }
        if (!useNamedConnections && (portIndexIfNoPortName < verilogInstance.connections.size())) {
            verilogConnection = verilogInstance.connections.get(portIndexIfNoPortName);
        }
        VerilogNet verilogNet = (verilogConnection == null) || verilogConnection.nets.isEmpty()
                ? null : verilogConnection.nets.get(0);

        return verilogNet == null ? null
                : new Signal(VerilogUtils.getNetBusSuffixName(verilogNet), Signal.Type.INTERNAL);
    }

    private FunctionComponent createAssignGate(Circuit circuit, VerilogAssign verilogAssign,
            HashMap<String, Net> signalToNetMap) throws DeserialisationException {

        final FunctionComponent component = new FunctionComponent();
        circuit.add(component);
        String netName = VerilogUtils.getNetBusSuffixName(verilogAssign.net);
        reparentAndRenameComponent(circuit, component, netName);

        AssignGate assignGate;
        if ((celementAssign && isCelementAssign(verilogAssign))
                || (sequentialAssign && isSequentialAssign(verilogAssign))) {

            assignGate = createSequentialAssignGate(verilogAssign);
        } else {
            assignGate = createCombinationalAssignGate(verilogAssign);
        }
        FunctionContact outContact = null;
        for (Map.Entry<String, String> connection : assignGate.connections.entrySet()) {
            FunctionContact contact = new FunctionContact();
            if (connection.getKey().equals(assignGate.outputName)) {
                contact.setIOType(IOType.OUTPUT);
                outContact = contact;
            } else {
                contact.setIOType(IOType.INPUT);
            }
            component.add(contact);
            if (connection.getKey() != null) {
                circuit.setName(contact, connection.getKey());
            }
            Net net = getOrCreateNet(connection.getValue(), signalToNetMap);
            if (net != null) {
                if (contact.isOutput()) {
                    net.sources.add(contact);
                } else {
                    net.sinks.add(contact);
                }
            }
        }

        if (outContact != null) {
            try {
                BooleanFormula setFormula = CircuitUtils.parsePinFunction(circuit, component, assignGate.setFunction);
                outContact.setSetFunctionQuiet(setFormula);
                BooleanFormula resetFormula = CircuitUtils.parsePinFunction(circuit, component, assignGate.resetFunction);
                outContact.setResetFunctionQuiet(resetFormula);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return component;
    }

    private boolean isSequentialAssign(VerilogAssign verilogAssign)
            throws DeserialisationException {

        String netName = VerilogUtils.getNetBusSuffixName(verilogAssign.net);
        String formula = VerilogUtils.getFormulaWithBusSuffixNames(verilogAssign.formula);
        Expression expression = convertFormulaToExpression(formula);
        LinkedList<Literal> literals = new LinkedList<>(expression.getLiterals());
        return literals.stream().anyMatch(literal -> (netName != null) && netName.equals(literal.name));
    }

    private boolean isCelementAssign(VerilogAssign verilogAssign) {
        try {
            BooleanFormula formula = BooleanFormulaParser.parse(VerilogUtils.getFormulaWithBusSuffixNames(verilogAssign.formula));
            List<BooleanVariable> variables = FormulaUtils.extractOrderedVariables(formula);
            if (variables.size() != 3) {
                return false;
            }
            String seqName = VerilogUtils.getNetBusSuffixName(verilogAssign.net);
            BooleanVariable aVar = null;
            BooleanVariable bVar = null;
            BooleanVariable seqVar = null;
            if (seqName != null) {
                for (BooleanVariable variable : variables) {
                    if (seqName.equals(variable.getLabel())) {
                        seqVar = variable;
                    } else {
                        if (aVar == null) {
                            aVar = variable;
                        } else {
                            bVar = variable;
                        }
                    }
                }
            }
            if ((aVar == null) || (bVar == null) || (seqVar == null)) {
                return false;
            }
            BddManager bddManager = new BddManager();
            return bddManager.isEquivalent(FormulaUtils.createMaj(aVar, bVar, seqVar), formula)
                    || bddManager.isEquivalent(FormulaUtils.createMaj(new Not(aVar), bVar, seqVar), formula)
                    || bddManager.isEquivalent(FormulaUtils.createMaj(aVar, new Not(bVar), seqVar), formula)
                    || bddManager.isEquivalent(FormulaUtils.createMaj(new Not(aVar), new Not(bVar), seqVar), formula);

        } catch (ParseException e) {
            return false;
        }
    }

    private AssignGate createCombinationalAssignGate(VerilogAssign verilogAssign)
            throws DeserialisationException {

        String formula = VerilogUtils.getFormulaWithBusSuffixNames(verilogAssign.formula);
        Expression expression = convertFormulaToExpression(formula);
        int index = 0;
        HashMap<String, String> connections = new HashMap<>(); // (port -> net)
        String outputName = VerilogUtils.getPrimitiveGatePinName(0);
        String netName = VerilogUtils.getNetBusSuffixName(verilogAssign.net);
        connections.put(outputName, netName);
        LinkedList<Literal> literals = new LinkedList<>(expression.getLiterals());
        Collections.reverse(literals);
        for (Literal literal : literals) {
            String literalName = literal.name;
            String name = VerilogUtils.getPrimitiveGatePinName(++index);
            literal.name = name;
            connections.put(name, literalName);
        }
        return new AssignGate(outputName, expression.toString(), null, connections);
    }

    private AssignGate createSequentialAssignGate(VerilogAssign verilogAssign)
            throws DeserialisationException {

        String formula = VerilogUtils.getFormulaWithBusSuffixNames(verilogAssign.formula);
        Expression expression = convertFormulaToExpression(formula);
        String function = expression.toString();

        String netName = VerilogUtils.getNetBusSuffixName(verilogAssign.net);
        String setFunction = ExpressionUtils.extractHeuristicSetFunction(function, netName);
        Expression setExpression = convertFormulaToExpression(setFunction);

        String resetFunction = ExpressionUtils.extractHeuristicResetFunction(function, netName);
        Expression resetExpression = convertFormulaToExpression(resetFunction);
        if (DebugCommonSettings.getVerboseImport()) {
            LogUtils.logMessage("Extracting SET and RESET from assign " + netName + " = " + formula
                    + TextUtils.getBulletpoint("Set function: " + setFunction)
                    + TextUtils.getBulletpoint("Reset function: " + resetFunction));
        }
        HashMap<String, String> connections = new HashMap<>();
        String outputName = VerilogUtils.getPrimitiveGatePinName(0);
        connections.put(outputName, netName);

        LinkedList<Literal> literals = new LinkedList<>();
        literals.addAll(setExpression.getLiterals());
        literals.addAll(resetExpression.getLiterals());
        Collections.reverse(literals);
        int index = 0;
        HashMap<String, String> netToPortMap = new HashMap<>();
        for (Literal literal : literals) {
            String literalName = literal.name;
            String name = netToPortMap.get(literalName);
            if (name == null) {
                name = VerilogUtils.getPrimitiveGatePinName(++index);
                netToPortMap.put(literalName, name);
            }
            literal.name = name;
            connections.put(name, literalName);
        }
        return new AssignGate(outputName, setExpression.toString(), resetExpression.toString(), connections);
    }

    private Expression convertFormulaToExpression(String formula)
            throws DeserialisationException {

        InputStream expressionStream = new ByteArrayInputStream(formula.getBytes(StandardCharsets.UTF_8));
        ExpressionParser expressionParser = new ExpressionParser(expressionStream);
        if (DebugCommonSettings.getParserTracing()) {
            expressionParser.enable_tracing();
        } else {
            expressionParser.disable_tracing();
        }
        try {
            return expressionParser.parseExpression();
        } catch (org.workcraft.plugins.circuit.jj.expression.ParseException |
                 org.workcraft.plugins.circuit.jj.expression.TokenMgrError e) {

            throw new DeserialisationException(e.getMessage()
                    + "\nCould not parse assign expression '" + formula + "'."
                    + "\nOnly '~', '&', and '|' operators are currently supported.");
        }
    }

    private HashMap<String, Net> createPorts(Circuit circuit, VerilogModule verilogModule, Collection<Mutex> mutexes)
            throws DeserialisationException {

        HashMap<String, Net> signalToNetMap = new HashMap<>();
        // Process mutex grants
        Set<String> grantSignals = new HashSet<>();
        for (Mutex mutex : mutexes) {
            if (mutex.g1 != null) {
                grantSignals.add(mutex.g1.name);
            }
            if (mutex.g2 != null) {
                grantSignals.add(mutex.g2.name);
            }
        }
        String locationPrefix = "In module '" + verilogModule.name + "' ";
        // Process inout ports
        List<String> inoutPortNames = verilogModule.ports.stream()
                .filter(VerilogPort::isInout)
                .map(verilogPort -> verilogPort.name)
                .sorted(SortUtils::compareNatural)
                .collect(Collectors.toList());

        if (!inoutPortNames.isEmpty()) {
            if (CircuitSettings.getAcceptInoutPort()) {
                if (DebugCommonSettings.getVerboseImport()) {
                    LogUtils.logMessage(TextUtils.wrapMessageWithItems(
                            locationPrefix + "ignored inout port", inoutPortNames));
                }
                inoutPortNames.forEach(portName -> signalToNetMap.put(portName, null));
            } else {
                throw new DeserialisationException(TextUtils.wrapMessageWithItems(
                        locationPrefix + "cannot accept inout port", inoutPortNames));
            }
        }

        // Process input and output ports
        for (VerilogPort verilogPort : verilogModule.ports) {
            if (verilogPort.isInput() || verilogPort.isOutput()) {
                List<String> portNetNames = getPortNetNames(verilogPort);
                if (verilogPort.range != null) {
                    if (DebugCommonSettings.getVerboseImport()) {
                        LogUtils.logMessage(locationPrefix + "bus " + verilogPort.name + verilogPort.range
                                + " is split into nets: " + String.join(", ", portNetNames));
                    }
                }
                for (String portNetName : portNetNames) {
                    // Input port that is a grant of factored-out mutex should be inserted as an output port
                    boolean isInputPort = verilogPort.isInput() && !grantSignals.contains(verilogPort.name);
                    createPort(circuit, signalToNetMap, portNetName, isInputPort);
                }
            }
        }
        return signalToNetMap;
    }

    private void createPort(Circuit circuit, HashMap<String, Net> signalToNetMap, String portName, boolean isInput) {
        FunctionContact contact = circuit.createNodeWithHierarchy(portName, circuit.getRoot(), FunctionContact.class);
        if (contact != null) {
            if (isInput) {
                contact.setIOType(IOType.INPUT);
            } else {
                contact.setIOType(IOType.OUTPUT);
            }
            Net net = getOrCreateNet(portName, signalToNetMap);
            if (net != null) {
                if (isInput) {
                    net.sources.add(contact);
                } else {
                    net.sinks.add(contact);
                }
            }
        }
    }

    private List<String> getPortNetNames(VerilogPort verilogPort) {
        List<String> result = new ArrayList<>();
        if (verilogPort.range == null) {
            result.add(verilogPort.name);
        } else {
            int first = verilogPort.range.getFirst();
            int second = verilogPort.range.getSecond();
            if (first < second) {
                for (int i = first; i <= second; i++) {
                    VerilogNet verilogNet = new VerilogNet(verilogPort.name, i);
                    result.add(VerilogUtils.getNetBusSuffixName(verilogNet));
                }
            } else {
                for (int i = first; i >= second; i--) {
                    VerilogNet verilogNet = new VerilogNet(verilogPort.name, i);
                    result.add(VerilogUtils.getNetBusSuffixName(verilogNet));
                }
            }
        }
        return result;
    }

    private Gate createPrimitiveGate(VerilogInstance verilogInstance) {
        String operator = VerilogUtils.getPrimitiveOperator(verilogInstance.moduleName);
        if (operator == null) {
            return null;
        }
        StringBuilder expression = new StringBuilder();
        int index;
        for (index = 0; index < verilogInstance.connections.size(); index++) {
            if (index > 0) {
                String pinName = VerilogUtils.getPrimitiveGatePinName(index);
                if (!expression.isEmpty()) {
                    expression.append(operator);
                }
                expression.append(pinName);
            }
        }
        if (!VerilogUtils.getPrimitivePolarity(verilogInstance.moduleName)) {
            if (index > 1) {
                expression = new StringBuilder("(" + expression + ")");
            }
            expression.insert(0, "!");
        }
        Function function = new Function(VerilogUtils.getPrimitiveGatePinName(0), expression.toString());
        return new Gate("", 0.0, function, null, true);
    }

    private FunctionComponent createLibraryGate(Circuit circuit, VerilogInstance verilogInstance,
            HashMap<String, Net> signalToNetMap, Gate gate, SubstitutionRule substitutionRule) {

        String msg = "Processing instance '" + verilogInstance.name + "' in module '" + circuit.getTitle() + "': ";
        FunctionComponent component = GenlibUtils.instantiateGate(gate, verilogInstance.name, circuit);
        List<FunctionContact> orderedContacts = getGateOrderedContacts(component);
        int index = -1;
        for (VerilogConnection verilogConnection : verilogInstance.connections) {
            index++;
            if (verilogConnection == null) {
                continue;
            }
            VerilogNet verilogNet = verilogConnection.nets.get(0);
            Net net = getOrCreateNet(VerilogUtils.getNetBusSuffixName(verilogNet), signalToNetMap);
            if (net == null) {
                continue;
            }

            String pinName = gate.isPrimitive() ? VerilogUtils.getPrimitiveGatePinName(index)
                    : SubstitutionUtils.getContactSubstitutionName(verilogConnection.name, substitutionRule, msg);

            Node node = pinName == null ? orderedContacts.get(index) : circuit.getNodeByReference(component, pinName);
            if (node instanceof FunctionContact contact) {
                if (contact.isInput()) {
                    net.sinks.add(contact);
                } else {
                    net.sources.add(contact);
                }
            }
        }
        return component;
    }

    private List<FunctionContact> getGateOrderedContacts(FunctionComponent component) {
        List<FunctionContact> result = new ArrayList<>();
        FunctionContact gateOutput = component.getGateOutput();
        if (gateOutput != null) {
            result.add(gateOutput);

            BooleanFormula setFunction = gateOutput.getSetFunction();
            for (BooleanVariable variable : FormulaUtils.extractOrderedVariables(setFunction)) {
                if (variable instanceof FunctionContact) {
                    result.add((FunctionContact) variable);
                }
            }
        }
        return result;
    }

    private FunctionComponent createModuleInstance(Circuit circuit, VerilogInstance verilogInstance,
            HashMap<String, Net> signalToNetMap, Collection<VerilogModule> verilogModules)
            throws DeserialisationException {

        final FunctionComponent component = new FunctionComponent();
        VerilogModule verilogModule = getVerilogModule(verilogModules, verilogInstance.moduleName);
        if (verilogModule == null) {
            component.setIsEnvironment(true);
        } else if (moduleToFileNameMap != null) {
            String path = moduleToFileNameMap.get(verilogModule);
            component.setRefinement(new FileReference(path));
        }
        component.setModule(verilogInstance.moduleName);
        circuit.add(component);
        try {
            circuit.setName(component, verilogInstance.name);
        } catch (ArgumentException e) {
            String componentRef = circuit.getNodeReference(component);
            LogUtils.logWarning("Cannot set name '" + verilogInstance.name + "' for component '" + componentRef + "'");
        }
        int portIndex = -1;
        for (VerilogConnection verilogConnection : verilogInstance.connections) {
            portIndex++;
            if (verilogConnection == null) {
                continue;
            }
            String instanceCreationError = "Cannot create instance '" + verilogInstance.name
                    + "' of module '" + verilogInstance.moduleName + "'";

            VerilogPort verilogPort = getPortByNameOrPosition(verilogModule, verilogConnection.name, portIndex);
            if (verilogPort == null) {
                String details = (verilogConnection.name == null)
                        ? ("no connection in position " + portIndex)
                        : ("no port named '" + verilogConnection.name + "'");

                throw new DeserialisationException(instanceCreationError + details);
            }
            String portName = verilogPort.name;
            VerilogPort.Range portRange = verilogPort.range;
            List<VerilogNet> verilogNets = new ArrayList<>(verilogConnection.nets);
            int netCount = verilogNets.size();
            if (netCount == 1) {
                VerilogNet verilogNet = verilogNets.get(0);
                Net net = getOrCreateNet(VerilogUtils.getNetBusSuffixName(verilogNet), signalToNetMap);
                if (net != null) {
                    FunctionContact contact = new FunctionContact();
                    updateContactTypeAndNetConnectivity(verilogPort, contact, net);
                    component.add(contact);
                    Integer contactBusIndex = portRange == null ? null : portRange.getBottomIndex();
                    String contactName = MatchingUtils.getSignalWithBusSuffix(portName, contactBusIndex);
                    circuit.setName(contact, contactName);
                }
            } else {
                int portSize = (portRange == null) ? netCount : portRange.getSize();
                if (netCount > portSize) {
                    String details = " bus of " + netCount + " nets does not fit port '" + portName
                            + "' of size " + portSize;

                    throw new DeserialisationException(instanceCreationError + details);
                }
                Collections.reverse(verilogNets);
                int step = (portRange == null) ? 1 : portRange.getStep();
                int bottomIndex = (portRange == null) ? 0 : portRange.getBottomIndex();
                for (int index = 0; index < netCount; ++index) {
                    VerilogNet verilogNet = verilogNets.get(index);
                    Net net = getOrCreateNet(VerilogUtils.getNetBusSuffixName(verilogNet), signalToNetMap);
                    if (net != null) {
                        FunctionContact contact = new FunctionContact();
                        updateContactTypeAndNetConnectivity(verilogPort, contact, net);
                        component.add(contact);
                        int contactBusIndex = bottomIndex + step * index;
                        String contactName = MatchingUtils.getSignalWithBusSuffix(portName, contactBusIndex);
                        circuit.setName(contact, contactName);
                    }
                }
            }
        }
        return component;
    }

    private VerilogPort getPortByNameOrPosition(VerilogModule verilogModule, String portName, int portIndex) {
        VerilogPort port = null;
        if (verilogModule == null) {
            port = new VerilogPort(portName, VerilogPort.Type.WIRE, null);
        } else {
            if (portName != null) {
                port = verilogModule.getPort(portName);
            } else if ((portIndex >= 0) && (portIndex < verilogModule.ports.size())) {
                port = verilogModule.ports.get(portIndex);
            }
        }
        return port;
    }

    private void updateContactTypeAndNetConnectivity(VerilogPort verilogPort, FunctionContact contact, Net net) {
        if ((verilogPort == null) || verilogPort.isWire()) {
            if (net != null) {
                net.undefined.add(contact);
            }
        } else if (verilogPort.isOutput()) {
            contact.setIOType(IOType.OUTPUT);
            if (net != null) {
                net.sources.add(contact);
            }
        } else if (verilogPort.isInput()) {
            contact.setIOType(IOType.INPUT);
            if (net != null) {
                net.sinks.add(contact);
            }
        }
    }

    private VerilogModule getVerilogModule(Collection<VerilogModule> verilogModules, String moduleName) {
        if ((moduleName != null) && (verilogModules != null)) {
            for (VerilogModule verilogModule : verilogModules) {
                if (moduleName.equals(verilogModule.name)) {
                    return verilogModule;
                }
            }
        }
        return null;
    }

    private void insertMutexes(Collection<Mutex> mutexes, Circuit circuit, HashMap<String, Net> signalToNetMap) {
        LinkedList<String> internalSignals = new LinkedList<>();
        for (Mutex instanceMutex : mutexes == null ? new ArrayList<Mutex>() : mutexes) {
            if (instanceMutex.g1.type == Signal.Type.INTERNAL) {
                internalSignals.add(instanceMutex.g1.name);
            }
            if (instanceMutex.g2.type == Signal.Type.INTERNAL) {
                internalSignals.add(instanceMutex.g2.name);
            }
            createMutex(circuit, instanceMutex, signalToNetMap);
            removeTemporaryOutput(circuit, signalToNetMap, instanceMutex.r1);
            removeTemporaryOutput(circuit, signalToNetMap, instanceMutex.r2);
        }
        if (!internalSignals.isEmpty()) {
            DialogUtils.showWarning("Mutex grants will be exposed as output ports: "
                    + String.join(", ", internalSignals) + ".\n\n"
                    + "This is necessary (due to technical reasons) for verification\n"
                    + "of a circuit with mutex against its environment STG.");
        }
    }

    private void removeTemporaryOutput(Circuit circuit, HashMap<String, Net> nets, Signal signal) {
        if (signal.type == Signal.Type.INTERNAL) {
            Node node = circuit.getNodeByReference(signal.name);
            if (node instanceof FunctionContact contact) {
                if (contact.isPort() && contact.isOutput()) {
                    if (DebugCommonSettings.getVerboseImport()) {
                        LogUtils.logMessage("Signal '" + signal.name + "' is restored as internal.");
                    }
                    circuit.remove(contact);
                    Net net = nets.get(signal.name);
                    if (net != null) {
                        net.sinks.remove(contact);
                        for (FunctionContact source : net.sources) {
                            if (source.getParent() instanceof FunctionComponent component) {
                                reparentAndRenameComponent(circuit, component, signal.name);
                            }
                        }
                    }
                }
            }
        }
    }

    private void reparentAndRenameComponent(Circuit circuit, FunctionComponent component, String ref) {
        String containerRef = NamespaceHelper.getParentReference(ref);
        if (!containerRef.isEmpty()) {
            PageNode container;
            Node parent = circuit.getNodeByReference(containerRef);
            if (parent instanceof PageNode) {
                container = (PageNode) parent;
            } else {
                container = circuit.createNodeWithHierarchy(containerRef, circuit.getRoot(), PageNode.class);
            }
            if (container != component.getParent()) {
                circuit.reparent(container, circuit, circuit.getRoot(), Collections.singletonList(component));
            }
        }
        String name = NamespaceHelper.getReferenceName(ref);
        circuit.setName(component, name);
    }

    private FunctionComponent createWait(Circuit circuit, Wait waitInstance, HashMap<String, Net> signalToNetMap) {
        if (waitInstance == null) {
            return null;
        }
        Wait.Type waitType = waitInstance.type;
        Wait waitModule = CircuitSettings.parseWaitData(waitType);
        if (waitModule == null) {
            LogUtils.logError(ArbitrationUtils.getMissingWaitMessage(waitType));
            return null;
        }

        FunctionComponent component = new FunctionComponent();
        circuit.add(component);
        component.setModule(waitModule.name);
        component.setIsArbitrationPrimitive(true);

        reparentAndRenameComponent(circuit, component, waitInstance.name);
        FunctionContact sigContact = addComponentPin(circuit, component, waitModule.sig, waitInstance.sig, signalToNetMap);
        FunctionContact ctrlContact = addComponentPin(circuit, component, waitModule.ctrl, waitInstance.ctrl, signalToNetMap);
        FunctionContact sanContact = addComponentPin(circuit, component, waitModule.san, waitInstance.san, signalToNetMap);

        ArbitrationUtils.assignWaitFunctions(waitInstance.type, sigContact, ctrlContact, sanContact);
        return component;
    }

    private FunctionComponent createMutex(Circuit circuit, Mutex mutexInstance, HashMap<String, Net> signalToNetMap) {
        if (mutexInstance == null) {
            return null;
        }
        Mutex.Protocol mutexProtocol = mutexInstance.protocol;
        Mutex mutexModule = CircuitSettings.parseMutexData(mutexProtocol);
        if (mutexModule == null) {
            LogUtils.logError(ArbitrationUtils.getMissingMutexMessage(mutexProtocol));
            return null;
        }

        FunctionComponent component = new FunctionComponent();
        circuit.add(component);
        component.setModule(mutexModule.name);
        component.setIsArbitrationPrimitive(true);

        reparentAndRenameComponent(circuit, component, mutexInstance.name);
        FunctionContact r1Contact = addComponentPin(circuit, component, mutexModule.r1, mutexInstance.r1, signalToNetMap);
        FunctionContact g1Contact = addComponentPin(circuit, component, mutexModule.g1, mutexInstance.g1, signalToNetMap);
        FunctionContact r2Contact = addComponentPin(circuit, component, mutexModule.r2, mutexInstance.r2, signalToNetMap);
        FunctionContact g2Contact = addComponentPin(circuit, component, mutexModule.g2, mutexInstance.g2, signalToNetMap);

        ArbitrationUtils.setMutexFunctionsQuiet(mutexProtocol, r1Contact, g1Contact, r2Contact, g2Contact);

        setMutexGrant(circuit, mutexInstance.g1, signalToNetMap);
        setMutexGrant(circuit, mutexInstance.g2, signalToNetMap);
        return component;
    }

    private void setMutexGrant(Circuit circuit, Signal signal, HashMap<String, Net> signalToNetMap) {
        Node node = (signal == null) || (signal.name == null) ? null : circuit.getNodeByReference(signal.name);
        if (node instanceof FunctionContact port) {
            switch (signal.type) {
                case INPUT -> port.setIOType(IOType.INPUT);
                case INTERNAL, OUTPUT -> port.setIOType(IOType.OUTPUT);
            }
            Net net = getOrCreateNet(signal.name, signalToNetMap);
            net.sinks.add(port);
        }
    }

    private FunctionContact addComponentPin(Circuit circuit, FunctionComponent component, Signal port,
            Signal signal, HashMap<String, Net> signalToNetMap) {

        FunctionContact contact = new FunctionContact();
        if (port.type == Signal.Type.INPUT) {
            contact.setIOType(IOType.INPUT);
        } else {
            contact.setIOType(IOType.OUTPUT);
        }
        component.add(contact);
        circuit.setName(contact, port.name);
        Net net = signal == null ? null : getOrCreateNet(signal.name, signalToNetMap);
        if (net != null) {
            if (port.type == Signal.Type.INPUT) {
                net.sinks.add(contact);
            } else {
                net.sources.add(contact);
            }
        }
        return contact;
    }

    private Net getOrCreateNet(String name, HashMap<String, Net> signalToNetMap) {
        Net net = null;
        if (name != null) {
            if (signalToNetMap.containsKey(name)) {
                net = signalToNetMap.get(name);
            } else {
                net = new Net();
                signalToNetMap.put(name, net);
            }
        }
        return net;
    }

    private void createConnections(Circuit circuit, Map<String, Net> signalToNetMap) {
        boolean finalised = false;
        while (!finalised) {
            finalised = true;
            for (Net net : signalToNetMap.values()) {
                if (net != null) {
                    finalised &= finaliseNet(circuit, net);
                }
            }
        }
        for (Net net : signalToNetMap.values()) {
            if (net != null) {
                createConnection(circuit, net);
            }
        }
    }

    private boolean finaliseNet(Circuit circuit, Net net) {
        boolean result = true;
        if (net.sources.isEmpty()) {
            if (net.undefined.size() == 1) {
                FunctionContact sourceContact = net.undefined.iterator().next();
                sourceContact.setIOType(sourceContact.isPort() ? IOType.INPUT : IOType.OUTPUT);
                net.sources.add(sourceContact);
                String sourceContactRef = circuit.getNodeReference(sourceContact);
                if (DebugCommonSettings.getVerboseImport()) {
                    LogUtils.logMessage("Source contact detected: " + sourceContactRef);
                }
                net.undefined.clear();
                result = false;
            } else {
                FunctionContact sourceContact = guessNetSource(circuit, net, new String[]{"O", "ON", "Y", "Z", "o"});
                if (sourceContact != null) {
                    sourceContact.setIOType(IOType.OUTPUT);
                    net.sources.add(sourceContact);
                    net.undefined.remove(sourceContact);
                    for (FunctionContact contact : new ArrayList<>(net.undefined)) {
                        if (contact.isPin()) {
                            contact.setIOType(IOType.INPUT);
                            net.sinks.add(contact);
                            net.undefined.remove(contact);
                        }
                    }
                }
            }
        } else if (!net.undefined.isEmpty()) {
            net.sinks.addAll(net.undefined);
            for (FunctionContact contact : net.undefined) {
                if (contact.isPort()) {
                    contact.setIOType(IOType.OUTPUT);
                } else {
                    contact.setIOType(IOType.INPUT);
                }
            }
            String contactRefs = ReferenceHelper.getNodesAsString(circuit, net.undefined);
            if (DebugCommonSettings.getVerboseImport()) {
                LogUtils.logMessage("Sink contacts detected: " + contactRefs);
            }
            net.undefined.clear();
            result = false;
        }
        return result;
    }

    private FunctionContact guessNetSource(Circuit circuit, Net net, String[] orderedCandidateOutputPinNames) {
        if (orderedCandidateOutputPinNames != null) {
            for (String outputPinName : orderedCandidateOutputPinNames) {
                FunctionContact contact = guessNetSource(circuit, net, outputPinName);
                if (contact != null) {
                    return contact;
                }
            }
        }
        return null;
    }

    private FunctionContact guessNetSource(Circuit circuit, Net net, String outputPinName) {
        if (outputPinName != null) {
            for (FunctionContact contact : net.undefined) {
                if (contact.isPin()) {
                    String contactName = circuit.getName(contact);
                    if (outputPinName.equals(contactName)) {
                        return contact;
                    }
                }
            }
        }
        return null;
    }

    private void createConnection(Circuit circuit, Net net) {
        String title = circuit.getTitle();
        String prefix = "In the imported module " + (title.isEmpty() ? "" : "'" + title + "' ");
        if (net.sources.size() != 1) {
            String sinksString = net.sinks.isEmpty() ? "" : TextUtils.getBulletpoint("sinks: "
                    + String.join(", ", ReferenceHelper.getReferenceSet(circuit, net.sinks)));

            String undefinedString = net.undefined.isEmpty() ? "" : TextUtils.getBulletpoint("undefined: "
                    + String.join(", ", ReferenceHelper.getReferenceSet(circuit, net.undefined)));

            if (net.sources.isEmpty()) {
                if (net.sinks.size() + net.undefined.size() > 1) {
                    LogUtils.logError(prefix + "net without source is removed"
                            + sinksString + undefinedString);
                }
            } else {
                String sourcesString = TextUtils.getBulletpoint("sources: "
                        + String.join(", ", ReferenceHelper.getReferenceSet(circuit, net.sources)));

                LogUtils.logError(prefix + "net with multiple sources is removed"
                        + sourcesString + sinksString + undefinedString);
            }
        } else {
            Contact sourceContact = net.sources.iterator().next();
            String sourceRef = circuit.getNodeReference(sourceContact);
            if (!net.undefined.isEmpty()) {
                Set<String> undefinedRefs = ReferenceHelper.getReferenceSet(circuit, net.undefined);
                LogUtils.logError(TextUtils.wrapMessageWithItems(prefix
                        + "net from contact '" + sourceRef + "' has undefined sink", undefinedRefs));
            }
            if (sourceContact.isPort() && sourceContact.isOutput()) {
                sourceContact.setIOType(IOType.INPUT);
                LogUtils.logWarning(prefix + "source contact '" + sourceRef + "' is changed to input port");
            }
            if (!sourceContact.isPort() && sourceContact.isInput()) {
                sourceContact.setIOType(IOType.OUTPUT);
                LogUtils.logWarning(prefix + "source contact '" + sourceRef + "' is changed to output pin");
            }
            for (FunctionContact sinkContact : net.sinks) {
                String sinkRef = circuit.getNodeReference(sinkContact);
                if (sinkContact.isPort() && sinkContact.isInput()) {
                    sinkContact.setIOType(IOType.OUTPUT);
                    LogUtils.logWarning(prefix + "sink contact '" + sinkRef + "' is changed to output port");
                }
                if (!sinkContact.isPort() && sinkContact.isOutput()) {
                    sinkContact.setIOType(IOType.INPUT);
                    LogUtils.logWarning(prefix + "sink contact '" + sinkRef + "' is changed to input pin");
                }
                try {
                    circuit.connect(sourceContact, sinkContact);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void setZeroDelayAttribute(HashMap<VerilogInstance, FunctionComponent> instanceComponentMap) {
        for (VerilogInstance verilogInstance : instanceComponentMap.keySet()) {
            FunctionComponent component = instanceComponentMap.get(verilogInstance);
            if ((component != null) && (verilogInstance.zeroDelay)) {
                try {
                    component.setIsZeroDelay(true);
                } catch (ArgumentException e) {
                    LogUtils.logWarning(e.getMessage()
                            + " Zero delay attribute is ignored for component '" + verilogInstance.name + "'.");
                }
            }
        }
    }

    private void setInitialStateForPragmas(Map<String, Net> signalToNetMap, Map<String, Boolean> signalStates) {
        // Set all signals first to 1 and then to 0, to make sure they switch and trigger the neighbours
        for (Net net : signalToNetMap.values()) {
            if (net != null) {
                for (Contact source : net.sources) {
                    source.setInitToOne(true);
                }
            }
        }
        for (Net net : signalToNetMap.values()) {
            if (net != null) {
                for (Contact source : net.sources) {
                    source.setInitToOne(false);
                }
            }
        }
        // Set all signals specified as high to 1
        for (String signalName : signalToNetMap.keySet()) {
            Boolean signalState = signalStates.get(signalName);
            Net net = signalToNetMap.get(signalName);
            if ((signalState != null) && (net != null)) {
                net.sources.forEach(source -> source.setInitToOne(signalState));
            }
        }
    }

    private Set<String> setInitialStateForConstants(Circuit circuit) {
        Set<String> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            Collection<FunctionContact> contacts = component.getFunctionContacts();
            FunctionContact onlyOutputPin = (contacts.size() != 1) ? null : contacts.iterator().next();
            if ((onlyOutputPin != null) && onlyOutputPin.isOutput()) {
                BooleanFormula setFunction = onlyOutputPin.getSetFunction();
                if (Zero.getInstance().equals(setFunction)) {
                    onlyOutputPin.setInitToOne(false);
                    result.add(VerilogUtils.calcAttachedNetName(circuit, onlyOutputPin));
                } else if (One.getInstance().equals(setFunction)) {
                    onlyOutputPin.setInitToOne(true);
                    result.add(VerilogUtils.calcAttachedNetName(circuit, onlyOutputPin));
                }
            }
        }
        return result;
    }

}
