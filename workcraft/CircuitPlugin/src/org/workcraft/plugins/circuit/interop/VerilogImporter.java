package org.workcraft.plugins.circuit.interop;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.*;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FormulaUtils;
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
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class VerilogImporter implements Importer {

    private static final String PRIMITIVE_GATE_INPUT_PREFIX = "i";
    private static final String PRIMITIVE_GATE_OUTPUT_NAME = "o";

    private final boolean celementAssign;
    private final boolean sequentialAssign;

    private Map<String, SubstitutionRule> substitutionRules = null;
    private Map<VerilogModule, String> moduleFileNames = null;

    private class AssignGate {
        public final String outputName;
        public final String setFunction;
        public final String resetFunction;
        public final HashMap<String, String> connections; // (portName -> wireName)

        AssignGate(String outputName, String setFunction, String resetFunction, HashMap<String, String> connections) {
            this.outputName = outputName;
            this.setFunction = setFunction;
            this.resetFunction = resetFunction;
            this.connections = connections;
        }
    }

    private class Wire {
        public FunctionContact source = null;
        public HashSet<FunctionContact> sinks = new HashSet<>();
        public HashSet<FunctionContact> undefined = new HashSet<>();
    }

    // Default constructor is required for PluginManager -- it is called via reflection.
    public VerilogImporter() {
        this(true, false);
    }

    public VerilogImporter(boolean celementAssign, boolean sequentialAssign) {
        this.celementAssign = celementAssign;
        this.sequentialAssign = sequentialAssign;
    }

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in)
            throws OperationCancelledException, DeserialisationException  {

        substitutionRules = null;
        moduleFileNames = null;
        Collection<VerilogModule> verilogModules = VerilogUtils.importVerilogModules(in);
        Circuit circuit = null;
        if ((verilogModules == null) || verilogModules.isEmpty()) {
            circuit = new Circuit();
        } else if (verilogModules.size() == 1) {
            VerilogModule verilogModule = verilogModules.iterator().next();
            circuit = createCircuit(verilogModule);
        } else {
            circuit = createCircuitHierarchy(verilogModules);
        }
        return new ModelEntry(new CircuitDescriptor(), circuit);
    }

    public Circuit createCircuit(VerilogModule verilogModule) {
        return createCircuit(verilogModule, Collections.emptySet());
    }

    public Circuit createCircuit(VerilogModule verilogModule, Collection<Mutex> mutexes) {
        return createCircuit(verilogModule, Collections.singletonList(verilogModule), mutexes);
    }

    private Circuit createCircuitHierarchy(Collection<VerilogModule> verilogModules)
            throws DeserialisationException, OperationCancelledException {

        if (Framework.getInstance().isInGuiMode()) {
            return createCircuitHierarchyGui(verilogModules);
        } else {
            return createCircuitHierarchyAuto(verilogModules);
        }
    }

    private Circuit createCircuitHierarchyGui(Collection<VerilogModule> verilogModules)
            throws OperationCancelledException {

        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        ImportVerilogDialog dialog = new ImportVerilogDialog(mainWindow, verilogModules);
        if (!dialog.reveal()) {
            throw new OperationCancelledException();
        }
        VerilogModule topVerilogModule = dialog.getTopModule();
        File dir = dialog.getDirectory();
        Set<VerilogModule> descendantModules = VerilogUtils.getDescendantModules(topVerilogModule, verilogModules);
        moduleFileNames = dialog.getModuleFileNames();
        Collection<String> badSaveFilePaths = getBadSaveFilePaths(descendantModules, dir);
        if (!badSaveFilePaths.isEmpty()) {
            String msg = "The following files already exist:\n"
                    + String.join("\n", badSaveFilePaths) + "\nOverwrite?";

            if (!DialogUtils.showConfirmWarning(msg, "Import hierarchical Verilog", false)) {
                throw new OperationCancelledException();
            }
        }
        return createCircuitHierarchy(topVerilogModule, descendantModules, dir);
    }

    private Circuit createCircuitHierarchyAuto(Collection<VerilogModule> verilogModules)
            throws DeserialisationException {

        VerilogModule topVerilogModule = VerilogUtils.getTopModule(verilogModules);
        Set<VerilogModule> descendantModules = VerilogUtils.getDescendantModules(topVerilogModule, verilogModules);
        File dir = Framework.getInstance().getWorkingDirectory();
        moduleFileNames = VerilogUtils.getModuleToFileMap(verilogModules);
        Collection<String> badSaveFilePaths = getBadSaveFilePaths(descendantModules, dir);
        if (!badSaveFilePaths.isEmpty()) {
            String msg = "Cannot import the circuit hierarchy because the following files already exist:\n"
                    + String.join("\n", badSaveFilePaths);

            LogUtils.logError(msg);
            throw new DeserialisationException("Clash of file names.");
        }
        return createCircuitHierarchy(topVerilogModule, descendantModules, dir);
    }

    private Collection<String> getBadSaveFilePaths(Set<VerilogModule> descendantModules, File dir) {
        Collection<String> result = new HashSet<>();
        for (VerilogModule module : descendantModules) {
            String fileName = moduleFileNames.get(module);
            File file = new File(dir, fileName);
            if (file.exists()) {
                result.add(file.getAbsolutePath());
            }
        }
        return result;
    }

    private Circuit createCircuitHierarchy(VerilogModule topVerilogModule,
            Collection<VerilogModule> descendantModules, File dir) {

        Circuit circuit = createCircuit(topVerilogModule, descendantModules, Collections.emptySet());

        Map<Circuit, String> circuitFileNames = new HashMap<>();
        for (VerilogModule verilogModule : descendantModules) {
            if (!verilogModule.isEmpty()) {
                Circuit descendantCircuit = createCircuit(verilogModule, descendantModules, Collections.emptySet());
                if (moduleFileNames != null) {
                    circuitFileNames.put(descendantCircuit, moduleFileNames.get(verilogModule));
                }
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
                File file = new File(dir, circuitFileNames.get(circuit));
                framework.saveWork(we, file);
            } catch (SerialisationException e) {
                e.printStackTrace();
            }
        }
        String base = FileUtils.getFullPath(dir);
        for (FunctionComponent component : topCircuit.getFunctionComponents()) {
            FileReference refinement = component.getRefinement();
            if (refinement != null) {
                refinement.setBase(base);
            }
        }
    }

    private Circuit createCircuit(VerilogModule verilogModule,
            Collection<VerilogModule> descendantModules, Collection<Mutex> mutexes) {

        Circuit circuit = new Circuit();
        circuit.setTitle(verilogModule.name);
        HashMap<VerilogInstance, FunctionComponent> instanceComponentMap = new HashMap<>();
        HashMap<String, Wire> wires = createPorts(circuit, verilogModule);
        for (VerilogAssign verilogAssign : verilogModule.assigns) {
            createAssignGate(circuit, verilogAssign, wires);
        }
        Mutex mutexModule = CircuitSettings.parseMutexData();
        for (VerilogInstance verilogInstance : verilogModule.instances) {
            SubstitutionRule substitutionRule = null;
            Gate gate = createPrimitiveGate(verilogInstance);
            if (gate == null) {
                Library library = LibraryManager.getLibrary();
                if (library != null) {
                    if (substitutionRules == null) {
                        substitutionRules = SubstitutionUtils.readImportSubstitutionRules();
                    }
                    substitutionRule = substitutionRules.get(verilogInstance.moduleName);
                    String msg = "Processing instance '" + verilogInstance.name + "' in module '" + verilogModule.name + "': ";
                    String gateName = SubstitutionUtils.getModuleSubstitutionName(verilogInstance.moduleName, substitutionRule, msg);
                    gate = library.get(gateName);
                }
            }
            FunctionComponent component = null;
            if (gate != null) {
                component = createLibraryGate(circuit, verilogInstance, wires, gate, substitutionRule);
            } else if (isMutexInstance(verilogInstance, mutexModule)) {
                Mutex mutexInstance = instanceToMutex(verilogInstance, mutexModule);
                component = createMutex(circuit, mutexInstance, mutexModule, wires);
            } else {
                component = createBlackBox(circuit, verilogInstance, wires, descendantModules);
            }
            if (component != null) {
                instanceComponentMap.put(verilogInstance, component);
            }
        }
        insertMutexes(mutexes, circuit, wires);
        createConnections(circuit, wires);
        setInitialState(wires, verilogModule.signalStates);
        setZeroDelayAttribute(instanceComponentMap);
        checkImportResult(circuit);
        return circuit;
    }

    private void checkImportResult(Circuit circuit) {
        String msg = "";
        if (circuit.getFunctionComponents().isEmpty()) {
            msg += "has no components";
        }
        Set<String> hangingSignals = VerificationUtils.getHangingSignals(circuit);
        if (!hangingSignals.isEmpty()) {
            if (!msg.isEmpty()) {
                msg += " and ";
            }
            msg += TextUtils.wrapMessageWithItems("hanging contact", hangingSignals);
        }
        if (!msg.isEmpty()) {
            DialogUtils.showWarning("The imported circuit " + msg);
        }
    }

    private boolean isMutexInstance(VerilogInstance verilogInstance, Mutex module) {
        return (module != null) && (module.name != null) && module.name.equals(verilogInstance.moduleName);
    }

    private Mutex instanceToMutex(VerilogInstance verilogInstance, Mutex module) {
        Signal r1 = getPinConnectedSignal(verilogInstance, module.r1.name, 0);
        Signal g1 = getPinConnectedSignal(verilogInstance, module.g1.name, 1);
        Signal r2 = getPinConnectedSignal(verilogInstance, module.r2.name, 2);
        Signal g2 = getPinConnectedSignal(verilogInstance, module.g2.name, 3);
        return new Mutex(verilogInstance.name, r1, g1, r2, g2);
    }

    private Signal getPinConnectedSignal(VerilogInstance verilogInstance, String portName, int portIndexIfNoPortName) {
        VerilogConnection verilogConnection = null;
        boolean useNamedConnections = true;
        for (VerilogConnection connection : verilogInstance.connections) {
            if (connection.name == null) {
                useNamedConnections = false;
                break;
            } else if (portName.equals(connection.name)) {
                verilogConnection = connection;
                break;
            }
        }
        if (!useNamedConnections && (portIndexIfNoPortName < verilogInstance.connections.size())) {
            verilogConnection = verilogInstance.connections.get(portIndexIfNoPortName);
        }
        return verilogConnection == null ? null : new Signal(getWireName(verilogConnection), Signal.Type.INTERNAL);
    }

    private FunctionComponent createAssignGate(Circuit circuit, VerilogAssign verilogAssign, HashMap<String, Wire> wires) {
        final FunctionComponent component = new FunctionComponent();
        circuit.add(component);
        reparentAndRenameComponent(circuit, component, verilogAssign.name);

        AssignGate assignGate = null;

        if ((celementAssign && isCelementAssign(verilogAssign))
                || (sequentialAssign && isSequentialAssign(verilogAssign))) {

            assignGate = createSequentialAssignGate(verilogAssign);
        } else {
            assignGate = createCombinationalAssignGate(verilogAssign);
        }
        FunctionContact outContact = null;
        for (Map.Entry<String, String> connection: assignGate.connections.entrySet()) {
            Wire wire = getOrCreateWire(connection.getValue(), wires);
            FunctionContact contact = new FunctionContact();
            if (connection.getKey().equals(assignGate.outputName)) {
                contact.setIOType(IOType.OUTPUT);
                outContact = contact;
                wire.source = contact;
            } else {
                contact.setIOType(IOType.INPUT);
                wire.sinks.add(contact);
            }
            component.add(contact);
            if (connection.getKey() != null) {
                circuit.setName(contact, connection.getKey());
            }
        }

        if (outContact != null) {
            try {
                BooleanFormula setFormula = CircuitUtils.parsePinFuncton(circuit, component, assignGate.setFunction);
                outContact.setSetFunctionQuiet(setFormula);
                BooleanFormula resetFormula = CircuitUtils.parsePinFuncton(circuit, component, assignGate.resetFunction);
                outContact.setResetFunctionQuiet(resetFormula);
            } catch (org.workcraft.formula.jj.ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return component;
    }

    private boolean isSequentialAssign(VerilogAssign verilogAssign) {
        Expression expression = convertStringToExpression(verilogAssign.formula);
        LinkedList<Literal> literals = new LinkedList<>(expression.getLiterals());
        return literals.stream().anyMatch(literal -> verilogAssign.name.equals(literal.name));
    }

    private boolean isCelementAssign(VerilogAssign verilogAssign) {
        try {
            BooleanFormula formula = BooleanFormulaParser.parse(verilogAssign.formula);
            List<BooleanVariable> variables = FormulaUtils.extractOrderedVariables(formula);
            if (variables.size() != 3) {
                return false;
            }
            if (!variables.stream().anyMatch(var -> verilogAssign.name.equals(var.getLabel()))) {
                return false;
            }
            BooleanVariable aVar = variables.get(0);
            BooleanVariable bVar = variables.get(1);
            BooleanVariable cVar = variables.get(2);
            return new BddManager().equal(FormulaUtils.createMaj(aVar, bVar, cVar), formula);
        } catch (ParseException e) {
        }
        return false;
    }

    private AssignGate createCombinationalAssignGate(VerilogAssign verilogAssign) {
        Expression expression = convertStringToExpression(verilogAssign.formula);
        int index = 0;
        HashMap<String, String> connections = new HashMap<>(); // (port -> net)
        String outputName = getPrimitiveGatePinName(0);
        connections.put(outputName, verilogAssign.name);
        LinkedList<Literal> literals = new LinkedList<>(expression.getLiterals());
        Collections.reverse(literals);
        for (Literal literal: literals) {
            String netName = literal.name;
            String name = getPrimitiveGatePinName(++index);
            literal.name = name;
            connections.put(name, netName);
        }
        return new AssignGate(outputName, expression.toString(), null, connections);
    }

    private AssignGate createSequentialAssignGate(VerilogAssign verilogAssign) {
        Expression expression = convertStringToExpression(verilogAssign.formula);
        String function = expression.toString();

        String setFunction = ExpressionUtils.extactSetExpression(function, verilogAssign.name);
        Expression setExpression = convertStringToExpression(setFunction);

        String resetFunction = ExpressionUtils.extactResetExpression(function, verilogAssign.name);
        Expression resetExpression = convertStringToExpression(resetFunction);
        if (DebugCommonSettings.getVerboseImport()) {
            LogUtils.logInfo("Extracting SET and RESET from assign " + verilogAssign.name + " = " + verilogAssign.formula);
            LogUtils.logInfo("  Function: " + function);
            LogUtils.logInfo("  Set function: " + setFunction);
            LogUtils.logInfo("  Reset function: " + resetFunction);
        }
        HashMap<String, String> connections = new HashMap<>();
        String outputName = getPrimitiveGatePinName(0);
        connections.put(outputName, verilogAssign.name);

        LinkedList<Literal> literals = new LinkedList<>();
        literals.addAll(setExpression.getLiterals());
        literals.addAll(resetExpression.getLiterals());
        Collections.reverse(literals);
        int index = 0;
        HashMap<String, String> netToPortMap = new HashMap<>();
        for (Literal literal: literals) {
            String netName = literal.name;
            String name = netToPortMap.get(netName);
            if (name == null) {
                name = getPrimitiveGatePinName(++index);
                netToPortMap.put(netName, name);
            }
            literal.name = name;
            connections.put(name, netName);
        }
        return new AssignGate(outputName, setExpression.toString(), resetExpression.toString(), connections);
    }

    private Expression convertStringToExpression(String formula) {
        InputStream expressionStream = new ByteArrayInputStream(formula.getBytes());
        ExpressionParser expressionParser = new ExpressionParser(expressionStream);
        if (DebugCommonSettings.getParserTracing()) {
            expressionParser.enable_tracing();
        } else {
            expressionParser.disable_tracing();
        }
        Expression expression = null;
        try {
            expression = expressionParser.parseExpression();
        } catch (org.workcraft.plugins.circuit.jj.expression.ParseException e) {
            LogUtils.logWarning("Could not parse assign expression '" + formula + "'.");
        }
        return expression;
    }

    private HashMap<String, Wire> createPorts(Circuit circuit, VerilogModule verilogModule) {
        HashMap<String, Wire> wires = new HashMap<>();
        for (VerilogPort verilogPort: verilogModule.ports) {
            List<String> portNames = getPortNames(verilogPort);
            if (verilogPort.range != null) {
                LogUtils.logInfo("Bus " + verilogPort.name + verilogPort.range + " is split to wires: "
                        + String.join(", ", portNames));
            }
            for (String portName: portNames) {
                createPort(circuit, wires, portName, verilogPort.isInput());
            }
        }
        return wires;
    }

    private void createPort(Circuit circuit, HashMap<String, Wire> wires, String portName, boolean isInput) {
        FunctionContact contact = circuit.createNodeWithHierarchy(portName, circuit.getRoot(), FunctionContact.class);
        Wire wire = getOrCreateWire(portName, wires);
        if (isInput) {
            contact.setIOType(IOType.INPUT);
            wire.source = contact;
        } else {
            contact.setIOType(IOType.OUTPUT);
            wire.sinks.add(contact);
        }
    }

    private List<String> getPortNames(VerilogPort verilogPort) {
        List<String> result = new ArrayList<>();
        if (verilogPort.range == null) {
            result.add(verilogPort.name);
        } else {
            int first = verilogPort.range.getFirst();
            int second = verilogPort.range.getSecond();
            if (first < second) {
                for (int i = first; i <= second; i++) {
                    result.add(verilogPort.name + getBusSuffix(i));
                }
            } else {
                for (int i = first; i >= second; i--) {
                    result.add(verilogPort.name + getBusSuffix(i));
                }
            }
        }
        return result;
    }

    private String getBusSuffix(Integer index) {
        return (index == null) ? "" : CircuitSettings.getBusSuffix().replace("$", Integer.toString(index));
    }

    private Gate createPrimitiveGate(VerilogInstance verilogInstance) {
        String operator = getPrimitiveOperator(verilogInstance.moduleName);
        if (operator == null) {
            return null;
        }
        String expression = "";
        int index;
        for (index = 0; index < verilogInstance.connections.size(); index++) {
            if (index > 0) {
                String pinName = getPrimitiveGatePinName(index);
                if (!expression.isEmpty()) {
                    expression += operator;
                }
                expression += pinName;
            }
        }
        if (isInvertingPrimitive(verilogInstance.moduleName)) {
            if (index > 1) {
                expression = "(" + expression + ")";
            }
            expression = "!" + expression;
        }
        Function function = new Function(PRIMITIVE_GATE_OUTPUT_NAME, expression);
        return new Gate("", 0.0, function, null, true);
    }

    private String getPrimitiveOperator(String primitiveName) {
        switch (primitiveName) {
        case "buf":
        case "not":
            return "";
        case "and":
        case "nand":
            return "*";
        case "or":
        case "nor":
            return "+";
        case "xnor":
            return "^";
        default:
            return null;
        }
    }

    private boolean isInvertingPrimitive(String primitiveName) {
        switch (primitiveName) {
        case "buf":
        case "and":
        case "or":
            return false;
        case "not":
        case "nand":
        case "nor":
        case "xnor":
            return true;
        default:
            return true;
        }
    }

    private String getPrimitiveGatePinName(int index) {
        if (index == 0) {
            return PRIMITIVE_GATE_OUTPUT_NAME;
        } else {
            return PRIMITIVE_GATE_INPUT_PREFIX + index;
        }
    }

    private FunctionComponent createLibraryGate(Circuit circuit, VerilogInstance verilogInstance,
            HashMap<String, Wire> wires, Gate gate, SubstitutionRule substitutionRule) {

        String msg = "Processing instance '" + verilogInstance.name + "' in module '" + circuit.getTitle() + "': ";
        FunctionComponent component = GenlibUtils.instantiateGate(gate, verilogInstance.name, circuit);
        List<FunctionContact> orderedContacts = getGateOrderedContacts(component);
        int index = -1;
        for (VerilogConnection verilogConnection : verilogInstance.connections) {
            index++;
            if (verilogConnection == null) {
                continue;
            }

            String wireName = getWireName(verilogConnection);
            if (wireName == null) {
                continue;
            }
            Wire wire = getOrCreateWire(wireName, wires);

            String pinName = gate.isPrimitive() ? getPrimitiveGatePinName(index)
                    : SubstitutionUtils.getContactSubstitutionName(verilogConnection.name, substitutionRule, msg);

            Node node = pinName == null ? orderedContacts.get(index) : circuit.getNodeByReference(component, pinName);
            if (node instanceof FunctionContact) {
                FunctionContact contact = (FunctionContact) node;
                if (contact.isInput()) {
                    wire.sinks.add(contact);
                } else {
                    wire.source = contact;
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

    private String getWireName(VerilogConnection verilogConnection) {
        return verilogConnection.netName + getBusSuffix(verilogConnection.netIndex);
    }

    private FunctionComponent createBlackBox(Circuit circuit, VerilogInstance verilogInstance,
            HashMap<String, Wire> wires, Collection<VerilogModule> verilogModules) {

        final FunctionComponent component = new FunctionComponent();
        VerilogModule verilogModule = getVerilogModule(verilogModules, verilogInstance.moduleName);
        if (verilogModule == null) {
            component.setIsEnvironment(true);
        } else if (moduleFileNames != null) {
            String path = moduleFileNames.get(verilogModule);
            FileReference refinement = new FileReference();
            refinement.setPath(path);
            component.setRefinement(refinement);
        }
        component.setModule(verilogInstance.moduleName);
        circuit.add(component);
        try {
            circuit.setName(component, verilogInstance.name);
        } catch (ArgumentException e) {
            String componentRef = circuit.getNodeReference(component);
            LogUtils.logWarning("Cannot set name '" + verilogInstance.name + "' for component '" + componentRef + "'.");
        }
        HashMap<String, VerilogPort> instancePorts = getModulePortMap(verilogModule);
        for (VerilogConnection verilogConnection : verilogInstance.connections) {
            if (verilogConnection == null) {
                continue;
            }
            VerilogPort verilogPort = instancePorts.get(verilogConnection.name);
            Wire wire = getOrCreateWire(verilogConnection.netName, wires);
            FunctionContact contact = new FunctionContact();
            if (verilogPort == null) {
                wire.undefined.add(contact);
            } else {
                if (verilogPort.isInput()) {
                    contact.setIOType(IOType.INPUT);
                    wire.sinks.add(contact);
                } else {
                    contact.setIOType(IOType.OUTPUT);
                    wire.source = contact;
                }
            }
            component.add(contact);
            if (verilogConnection.name != null) {
                circuit.setName(contact, verilogConnection.name);
            }
        }
        return component;
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

    private void insertMutexes(Collection<Mutex> mutexes, Circuit circuit, HashMap<String, Wire> wires) {
        LinkedList<String> internalSignals = new LinkedList<>();
        if ((mutexes != null) & !mutexes.isEmpty()) {
            Mutex moduleMutex = CircuitSettings.parseMutexData();
            if ((moduleMutex != null) && (moduleMutex.name != null)) {
                for (Mutex instanceMutex : mutexes) {
                    if (instanceMutex.g1.type == Signal.Type.INTERNAL) {
                        internalSignals.add(instanceMutex.g1.name);
                    }
                    if (instanceMutex.g2.type == Signal.Type.INTERNAL) {
                        internalSignals.add(instanceMutex.g2.name);
                    }
                    createMutex(circuit, instanceMutex, moduleMutex, wires);
                    removeTemporaryOutput(circuit, wires, instanceMutex.r1);
                    removeTemporaryOutput(circuit, wires, instanceMutex.r2);
                }
            }
        }
        if (!internalSignals.isEmpty()) {
            DialogUtils.showWarning("Mutex grants will be exposed as output ports: "
                    + String.join(", ", internalSignals) + ".\n\n"
                    + "This is necessary (due to technical reasons) for verification\n"
                    + "of a circuit with mutex against its environment STG.");
        }
    }

    private void removeTemporaryOutput(Circuit circuit, HashMap<String, Wire> wires, Signal signal) {
        if (signal.type == Signal.Type.INTERNAL) {
            Node node = circuit.getNodeByReference(signal.name);
            if (node instanceof FunctionContact) {
                FunctionContact contact = (FunctionContact) node;
                if (contact.isPort() && contact.isOutput()) {
                    LogUtils.logInfo("Signal '" + signal.name + "' is restored as internal.");
                    circuit.remove(contact);
                    Wire wire = wires.get(signal.name);
                    if (wire != null) {
                        wire.sinks.remove(contact);
                        if ((wire.source != null) && (wire.source.getParent() instanceof FunctionComponent)) {
                            FunctionComponent component = (FunctionComponent) wire.source.getParent();
                            reparentAndRenameComponent(circuit, component, signal.name);
                        }
                    }
                }
            }
        }
    }

    private void reparentAndRenameComponent(Circuit circuit, FunctionComponent component, String ref) {
        String containerRef = NamespaceHelper.getParentReference(ref);
        if (!containerRef.isEmpty()) {
            PageNode container = null;
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
        HierarchyReferenceManager refManager = circuit.getReferenceManager();
        NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(component);
        NameManager nameManager = refManager.getNameManager(namespaceProvider);
        String name = NamespaceHelper.getReferenceName(ref);
        String derivedName = nameManager.getDerivedName(component, name);
        circuit.setName(component, derivedName);
    }

    private FunctionComponent createMutex(Circuit circuit, Mutex instance, Mutex module, HashMap<String, Wire> wires) {
        final FunctionComponent component = new FunctionComponent();
        component.setModule(module.name);
        circuit.add(component);
        reparentAndRenameComponent(circuit, component, instance.name);
        FunctionContact r1Contact = addMutexPin(circuit, component, module.r1, instance.r1, wires);
        FunctionContact g1Contact = addMutexPin(circuit, component, module.g1, instance.g1, wires);
        FunctionContact r2Contact = addMutexPin(circuit, component, module.r2, instance.r2, wires);
        FunctionContact g2Contact = addMutexPin(circuit, component, module.g2, instance.g2, wires);

        BooleanFormula g1Set = MutexUtils.getGrantSet(r1Contact, g2Contact, r2Contact);
        g1Contact.setSetFunctionQuiet(g1Set);

        BooleanFormula g1Reset = MutexUtils.getGrantReset(r1Contact);
        g1Contact.setResetFunctionQuiet(g1Reset);

        BooleanFormula g2Set = MutexUtils.getGrantSet(r2Contact, g1Contact, r1Contact);
        g2Contact.setSetFunctionQuiet(g2Set);
        BooleanFormula g2Reset = MutexUtils.getGrantReset(r2Contact);
        g2Contact.setResetFunctionQuiet(g2Reset);

        setMutexGrant(circuit, instance.g1, wires);
        setMutexGrant(circuit, instance.g2, wires);
        return component;
    }

    private void setMutexGrant(Circuit circuit, Signal signal, HashMap<String, Wire> wires) {
        Node node = circuit.getNodeByReference(signal.name);
        if (node instanceof FunctionContact) {
            FunctionContact port = (FunctionContact) node;
            switch (signal.type) {
            case INPUT:
                port.setIOType(IOType.INPUT);
                break;
            case INTERNAL:
            case OUTPUT:
                port.setIOType(IOType.OUTPUT);
                break;
            }
            Wire wire = getOrCreateWire(signal.name, wires);
            wire.sinks.add(port);
        }
    }

    private FunctionContact addMutexPin(Circuit circuit, FunctionComponent component, Signal port,
            Signal signal, HashMap<String, Wire> wires) {

        FunctionContact contact = new FunctionContact();
        if (port.type == Signal.Type.INPUT) {
            contact.setIOType(IOType.INPUT);
        } else {
            contact.setIOType(IOType.OUTPUT);
        }
        component.add(contact);
        circuit.setName(contact, port.name);
        Wire wire = getOrCreateWire(signal.name, wires);
        if (port.type == Signal.Type.INPUT) {
            wire.sinks.add(contact);
        } else {
            wire.source = contact;
        }
        return contact;
    }

    private Wire getOrCreateWire(String name, HashMap<String, Wire> wires) {
        Wire wire = wires.get(name);
        if (wire == null) {
            wire = new Wire();
            wires.put(name, wire);
        }
        return wire;
    }

    private void createConnections(Circuit circuit, Map<String, Wire> wires) {
        boolean finalised = false;
        while (!finalised) {
            finalised = true;
            for (Wire wire: wires.values()) {
                finalised &= finaliseWire(circuit, wire);
            }
        }
        for (Wire wire: wires.values()) {
            createConnection(circuit, wire);
        }
    }

    private boolean finaliseWire(Circuit circuit, Wire wire) {
        boolean result = true;
        if (wire.source == null) {
            if (wire.undefined.size() == 1) {
                wire.source = wire.undefined.iterator().next();
                if (wire.source.isPort()) {
                    wire.source.setIOType(IOType.INPUT);
                } else {
                    wire.source.setIOType(IOType.OUTPUT);
                }
                String contactRef = circuit.getNodeReference(wire.source);
                LogUtils.logInfo("Source contact detected: " + contactRef);
                wire.undefined.clear();
                result = false;
            }
        } else if (!wire.undefined.isEmpty()) {
            wire.sinks.addAll(wire.undefined);
            for (FunctionContact contact: wire.undefined) {
                if (contact.isPort()) {
                    contact.setIOType(IOType.OUTPUT);
                } else {
                    contact.setIOType(IOType.INPUT);
                }
            }
            String contactRefs = ReferenceHelper.getNodesAsString(circuit, wire.undefined);
            LogUtils.logInfo("Sink contacts detected: " + contactRefs);
            wire.undefined.clear();
            result = false;
        }
        return result;
    }

    private void createConnection(Circuit circuit, Wire wire) {
        Contact sourceContact = wire.source;
        if (sourceContact == null) {
            HashSet<FunctionContact> contacts = new HashSet<>();
            contacts.addAll(wire.sinks);
            contacts.addAll(wire.undefined);
            if (!contacts.isEmpty()) {
                String contactRefs = ReferenceHelper.getNodesAsString(circuit, wire.undefined);
                LogUtils.logError("Wire without a source is connected to the following contacts: " + contactRefs);
            }
        } else {
            String sourceRef = circuit.getNodeReference(sourceContact);
            if (!wire.undefined.isEmpty()) {
                String contactRefs = ReferenceHelper.getNodesAsString(circuit, wire.undefined);
                LogUtils.logError("Wire from contact '" + sourceRef + "' has undefined sinks: " + contactRefs);
            }
            if (sourceContact.isPort() && sourceContact.isOutput()) {
                sourceContact.setIOType(IOType.INPUT);
                LogUtils.logWarning("Source contact '" + sourceRef + "' is changed to input port.");
            }
            if (!sourceContact.isPort() && sourceContact.isInput()) {
                sourceContact.setIOType(IOType.OUTPUT);
                LogUtils.logWarning("Source contact '" + sourceRef + "' is changed to output pin.");
            }
            for (FunctionContact sinkContact: wire.sinks) {
                if (sinkContact.isPort() && sinkContact.isInput()) {
                    sinkContact.setIOType(IOType.OUTPUT);
                    LogUtils.logWarning("Sink contact '" + circuit.getNodeReference(sinkContact) + "' is changed to output port.");
                }
                if (!sinkContact.isPort() && sinkContact.isOutput()) {
                    sinkContact.setIOType(IOType.INPUT);
                    LogUtils.logWarning("Sink contact '" + circuit.getNodeReference(sinkContact) + "' is changed to input pin.");
                }
                try {
                    circuit.connect(sourceContact, sinkContact);
                } catch (InvalidConnectionException e) {
                }
            }
        }
    }

    private void setZeroDelayAttribute(HashMap<VerilogInstance, FunctionComponent> instanceComponentMap) {
        for (VerilogInstance verilogInstance: instanceComponentMap.keySet()) {
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

    private void setInitialState(Map<String, Wire> wires, Map<String, Boolean> signalStates) {
        // Set all signals first to 1 and then to 0, to make sure a switch and initiates switching of the neighbours.
        for (String signalName: wires.keySet()) {
            Wire wire = wires.get(signalName);
            if (wire.source != null) {
                wire.source.setInitToOne(true);
            }
        }
        for (String signalName: wires.keySet()) {
            Wire wire = wires.get(signalName);
            if (wire.source != null) {
                wire.source.setInitToOne(false);
            }
        }
        // Set all signals specified as high to 1.
        if (signalStates != null) {
            for (String signalName: wires.keySet()) {
                Wire wire = wires.get(signalName);
                if ((wire.source != null) && signalStates.containsKey(signalName)) {
                    boolean signalState = signalStates.get(signalName);
                    wire.source.setInitToOne(signalState);
                }
            }
        }
    }

    private HashMap<String, VerilogPort> getModulePortMap(VerilogModule verilogModule) {
        HashMap<String, VerilogPort> result = new HashMap<>();
        if (verilogModule != null) {
            for (VerilogPort verilogPort : verilogModule.ports) {
                result.put(verilogPort.name, verilogPort);
            }
        }
        return result;
    }

}
