package org.workcraft.plugins.circuit.interop;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.formula.utils.StringGenerator;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.expression.Expression;
import org.workcraft.plugins.circuit.expression.ExpressionUtils;
import org.workcraft.plugins.circuit.expression.Literal;
import org.workcraft.plugins.circuit.genlib.Function;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.jj.expression.ExpressionParser;
import org.workcraft.plugins.circuit.jj.verilog.VerilogParser;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.StructureUtilsKt;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.circuit.verilog.*;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class VerilogImporter implements Importer {

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

    private static final String MSG_MANY_TOP_MODULES = "More than one top module is found.";
    private static final String MSG_NO_TOP_MODULE = "No top module found.";

    private static final String PRIMITIVE_GATE_INPUT_PREFIX = "i";
    private static final String PRIMITIVE_GATE_OUTPUT_NAME = "o";

    private final boolean sequentialAssign;

    private class Wire {
        public FunctionContact source = null;
        public HashSet<FunctionContact> sinks = new HashSet<>();
        public HashSet<FunctionContact> undefined = new HashSet<>();
    }

    // Default constructor is required for PluginManager -- it is called via reflection.
    public VerilogImporter() {
        this(false);
    }

    public VerilogImporter(boolean sequentialAssign) {
        this.sequentialAssign = sequentialAssign;
    }

    @Override
    public VerilogFormat getFormat() {
        return VerilogFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        return new ModelEntry(new CircuitDescriptor(), importCircuit(in));
    }

    public Circuit importCircuit(InputStream in) throws DeserialisationException {
        return importCircuit(in, new LinkedList<>());
    }

    public Circuit importCircuit(InputStream in, Collection<Mutex> mutexes) throws DeserialisationException {
        try {
            VerilogParser parser = new VerilogParser(in);
            if (CommonDebugSettings.getParserTracing()) {
                parser.enable_tracing();
            } else {
                parser.disable_tracing();
            }
            HashMap<String, VerilogModule> modules = getModuleMap(parser.parseCircuit());
            HashSet<VerilogModule> topVerilogModules = getTopModule(modules);
            if (topVerilogModules.size() == 0) {
                throw new DeserialisationException(MSG_NO_TOP_MODULE);
            }
            if (topVerilogModules.size() > 1) {
                throw new DeserialisationException(MSG_MANY_TOP_MODULES);
            }
            if (CommonDebugSettings.getVerboseImport()) {
                LogUtils.logInfo("Parsed Verilog modules");
                for (VerilogModule verilogModule : modules.values()) {
                    if (topVerilogModules.contains(verilogModule)) {
                        System.out.print("// Top module\n");
                    }
                    printModule(verilogModule);
                }
            }
            VerilogModule topVerilogModule = topVerilogModules.iterator().next();
            return createCircuit(topVerilogModule, modules, mutexes);
        } catch (FormatException | org.workcraft.plugins.circuit.jj.verilog.ParseException e) {
            throw new DeserialisationException(e);
        }
    }

    private HashSet<VerilogModule> getTopModule(HashMap<String, VerilogModule> modules) {
        HashSet<VerilogModule> result = new HashSet<>(modules.values());
        if (modules.size() > 1) {
            for (VerilogModule verilogModule : modules.values()) {
                if (verilogModule.isEmpty()) {
                    result.remove(verilogModule);
                }
                for (VerilogInstance verilogInstance : verilogModule.instances) {
                    if (verilogInstance.moduleName == null) continue;
                    result.remove(modules.get(verilogInstance.moduleName));
                }
            }
        }
        return result;
    }

    private void printModule(VerilogModule verilogModule) {
        String portNames = verilogModule.ports.stream()
                .map(verilogPort -> verilogPort.name)
                .collect(Collectors.joining(", "));
        System.out.println("module " + verilogModule.name + " (" + portNames + ");");
        for (VerilogPort verilogPort : verilogModule.ports) {
            System.out.println("    " + verilogPort.type + " " + ((verilogPort.range == null) ? "" : verilogPort.range + " ") + verilogPort.name + ";");
        }
        for (VerilogAssign verilogAssign : verilogModule.assigns) {
            System.out.println("    assign " + verilogAssign.name + " = " + verilogAssign.formula + ";");
        }

        for (VerilogInstance verilogInstance : verilogModule.instances) {
            String instanceName = (verilogInstance.name == null) ? "" : verilogInstance.name;
            String pinNames = verilogInstance.connections.stream()
                    .map(verilogConnection -> getConnectionString(verilogConnection))
                    .collect(Collectors.joining(", "));
            System.out.println("    " + verilogInstance.moduleName + " " + instanceName + " (" + pinNames + ");");
        }
        System.out.println("endmodule\n");
    }

    private String getConnectionString(VerilogConnection verilogConnection) {
        String result = verilogConnection.netName + ((verilogConnection.netIndex == null) ? "" : "[" + verilogConnection.netIndex + "]");
        if (verilogConnection.name != null) {
            result = "." + verilogConnection.name + "(" + result + ")";
        }
        return result;
    }

    private Circuit createCircuit(VerilogModule topVerilogModule, HashMap<String, VerilogModule> modules, Collection<Mutex> mutexes) {
        Circuit circuit = new Circuit();
        circuit.setTitle(topVerilogModule.name);
        HashMap<VerilogInstance, FunctionComponent> instanceComponentMap = new HashMap<>();
        HashMap<String, Wire> wires = createPorts(circuit, topVerilogModule);
        for (VerilogAssign verilogAssign : topVerilogModule.assigns) {
            createAssignGate(circuit, verilogAssign, wires);
        }
        Mutex mutexModule = CircuitSettings.parseMutexData();
        Library library = null;
        for (VerilogInstance verilogInstance: topVerilogModule.instances) {
            Gate gate = createPrimitiveGate(verilogInstance);
            if (gate == null) {
                if (library == null) {
                    String libraryFileName = CircuitSettings.getGateLibrary();
                    library = GenlibUtils.readLibrary(libraryFileName);
                }
                gate = library.get(verilogInstance.moduleName);
            }
            FunctionComponent component = null;
            if (gate != null) {
                component = createLibraryGate(circuit, verilogInstance, wires, gate);
            } else if (isMutexInstance(verilogInstance, mutexModule)) {
                Mutex mutexInstance = instanceToMutex(verilogInstance, mutexModule);
                component = createMutex(circuit, mutexInstance, mutexModule, wires);
            } else {
                component = createBlackBox(circuit, verilogInstance, wires, modules);
            }
            if (component != null) {
                instanceComponentMap.put(verilogInstance, component);
            }
        }
        insertMutexes(mutexes, circuit, wires);
        createConnections(circuit, wires);
        setInitialState(circuit, wires, topVerilogModule.signalStates);
        setZeroDelayAttribute(instanceComponentMap);
        mergeGroups(circuit, topVerilogModule.groups, instanceComponentMap);
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
            msg += LogUtils.getTextWithRefs("hanging contact", hangingSignals);
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
        if (sequentialAssign && isSequentialAssign(verilogAssign)) {
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
        for (Literal literal: literals) {
            if (verilogAssign.name.equals(literal.name)) {
                return true;
            }
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
        if (CommonDebugSettings.getVerboseImport()) {
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
        if (CommonDebugSettings.getParserTracing()) {
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
            HashMap<String, Wire> wires, Gate gate) {
        FunctionComponent component = GenlibUtils.instantiateGate(gate, verilogInstance.name, circuit);
        int index = 0;
        for (VerilogConnection verilogConnection : verilogInstance.connections) {
            String wireName = getWireName(verilogConnection);
            Wire wire = getOrCreateWire(wireName, wires);
            String pinName = gate.isPrimitive() ? getPrimitiveGatePinName(index++) : verilogConnection.name;
            Node node = circuit.getNodeByReference(component, pinName);
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

    private String getWireName(VerilogConnection verilogConnection) {
        return verilogConnection.netName + getBusSuffix(verilogConnection.netIndex);
    }

    private FunctionComponent createBlackBox(Circuit circuit, VerilogInstance verilogInstance,
            HashMap<String, Wire> wires, HashMap<String, VerilogModule> modules) {
        final FunctionComponent component = new FunctionComponent();
        component.setModule(verilogInstance.moduleName);
        component.setIsEnvironment(true);
        circuit.add(component);
        try {
            circuit.setName(component, verilogInstance.name);
        } catch (ArgumentException e) {
            String componentRef = circuit.getNodeReference(component);
            LogUtils.logWarning("Cannot set name '" + verilogInstance.name + "' for component '" + componentRef + "'.");
        }
        VerilogModule verilogModule = modules.get(verilogInstance.moduleName);
        HashMap<String, VerilogPort> instancePorts = getModulePortMap(verilogModule);
        for (VerilogConnection verilogConnection : verilogInstance.connections) {
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

    private void insertMutexes(Collection<Mutex> mutexes, Circuit circuit, HashMap<String, Wire> wires) {
        LinkedList<String> internalSignals = new LinkedList<>();
        if (!mutexes.isEmpty()) {
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
                circuit.reparent(container, circuit, circuit.getRoot(), Arrays.asList(component));
            }
        }
        String name = NamespaceHelper.getReferenceName(ref);
        try {
            HierarchyReferenceManager refManager = circuit.getReferenceManager();
            NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(component);
            NameManager nameManagerer = refManager.getNameManager(namespaceProvider);
            String derivedName = nameManagerer.getDerivedName(component, name);
            circuit.setName(component, derivedName);
        } catch (ArgumentException e) {
            String componentRef = circuit.getNodeReference(component);
            LogUtils.logWarning("Cannot set name '" + ref + "' for component '" + componentRef + "'.");
        }
    }

    private FunctionComponent createMutex(Circuit circuit, Mutex instance, Mutex module, HashMap<String, Wire> wires) {
        final FunctionComponent component = new FunctionComponent();
        component.setModule(module.name);
        circuit.add(component);
        reparentAndRenameComponent(circuit, component, instance.name);
        addMutexPin(circuit, component, module.r1, instance.r1, wires);
        FunctionContact g1Contact = addMutexPin(circuit, component, module.g1, instance.g1, wires);
        addMutexPin(circuit, component, module.r2, instance.r2, wires);
        FunctionContact g2Contact = addMutexPin(circuit, component, module.g2, instance.g2, wires);
        try {
            setMutexFunctions(circuit, component, g1Contact, module.r1.name, module.r2.name, module.g2.name);
            setMutexFunctions(circuit, component, g2Contact, module.r2.name, module.r1.name, module.g1.name);
        } catch (org.workcraft.formula.jj.ParseException e) {
            throw new RuntimeException(e);
        }
        setMutexGrant(circuit, instance.g1, wires);
        setMutexGrant(circuit, instance.g2, wires);
        return component;
    }

    private void setMutexFunctions(Circuit circuit, final FunctionComponent component, FunctionContact grantContact,
            String reqPinName, String otherReqPinName, String otherGrantPinName) throws org.workcraft.formula.jj.ParseException {
        String setString = reqPinName + " * " + otherGrantPinName + "'";
        if (CircuitSettings.getMutexProtocol() == Mutex.Protocol.RELAXED) {
            setString += " + " + reqPinName + " * " + otherReqPinName + "'";
        }
        BooleanFormula setFormula = CircuitUtils.parsePinFuncton(circuit, component, setString);
        grantContact.setSetFunctionQuiet(setFormula);
        String resetString = reqPinName + "'";
        BooleanFormula resetFormula = CircuitUtils.parsePinFuncton(circuit, component, resetString);
        grantContact.setResetFunctionQuiet(resetFormula);
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
                port.setIOType(IOType.OUTPUT);
                break;
            case OUTPUT:
                port.setIOType(IOType.OUTPUT);
                break;
            }
            Wire wire = getOrCreateWire(signal.name, wires);
            wire.sinks.add(port);
        }
    }

    private FunctionContact addMutexPin(Circuit circuit, FunctionComponent component, Signal port, Signal signal,
            HashMap<String, Wire> wires) {
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
                    LogUtils.logWarning("Component '" + verilogInstance.name + "': " + e.getMessage());
                }
            }
        }
    }

    private void setInitialState(Circuit circuit, Map<String, Wire> wires, Map<String, Boolean> signalStates) {
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

    private HashMap<String, VerilogModule> getModuleMap(List<VerilogModule> verilogModules) {
        HashMap<String, VerilogModule> result = new HashMap<>();
        for (VerilogModule verilogModule : verilogModules) {
            if ((verilogModule == null) || (verilogModule.name == null)) continue;
            result.put(verilogModule.name, verilogModule);
        }
        return result;
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

    private void mergeGroups(Circuit circuit, Set<List<VerilogInstance>> groups, HashMap<VerilogInstance, FunctionComponent> instanceComponentMap) {
        for (List<VerilogInstance> group: groups) {
            HashSet<FunctionComponent> components = new HashSet<>();
            FunctionComponent rootComponent = null;
            for (VerilogInstance verilogInstance : group) {
                FunctionComponent component = instanceComponentMap.get(verilogInstance);
                if (component != null) {
                    components.add(component);
                    rootComponent = component;
                }
            }
            FunctionComponent complexComponent = mergeComponents(circuit, rootComponent, components);
            for (FunctionComponent component: components) {
                if (component == complexComponent) continue;
                circuit.remove(component);
            }
            // Prefix all the pins with underscore so there is no name clash on the subsequent round of renaming
            for (Contact contact: complexComponent.getContacts()) {
                circuit.setName(contact, "_" + contact.getName());
            }
            // Compact all the pins names
            int index = 0;
            for (Contact contact: complexComponent.getContacts()) {
                if (contact.isOutput()) {
                    circuit.setName(contact, getPrimitiveGatePinName(0));
                } else {
                    circuit.setName(contact, getPrimitiveGatePinName(++index));
                }
            }
        }
    }

    private FunctionComponent mergeComponents(Circuit circuit, FunctionComponent rootComponent, HashSet<FunctionComponent> components) {
        boolean done = false;
        do {
            HashSet<FunctionComponent> leafComponents = new HashSet<>();
            for (FunctionComponent component: components) {
                if (component == rootComponent) continue;
                for (MathNode node: StructureUtilsKt.getPostsetComponents(circuit, component)) {
                    if (node != rootComponent) continue;
                    leafComponents.add(component);
                }
            }
            if (leafComponents.isEmpty()) {
                done = true;
            } else {
                FunctionComponent newComponent = mergeLeafComponents(circuit, rootComponent, leafComponents);
                components.remove(rootComponent);
                circuit.remove(rootComponent);
                rootComponent = newComponent;
            }
        } while (!done);
        return rootComponent;
    }

    private FunctionComponent mergeLeafComponents(Circuit circuit, FunctionComponent rootComponent, HashSet<FunctionComponent> leafComponents) {
        FunctionComponent component = null;
        FunctionContact rootOutputContact = getOutputContact(circuit, rootComponent);
        List<Contact> rootInputContacts = new LinkedList<>(rootComponent.getInputs());

        HashMap<Contact, Contact> newToOldContactMap = new HashMap<>();
        component = new FunctionComponent();
        circuit.add(component);
        FunctionContact outputContact = new FunctionContact(IOType.OUTPUT);
        outputContact.setInitToOne(rootOutputContact.getInitToOne());
        component.add(outputContact);
        circuit.setName(outputContact, PRIMITIVE_GATE_OUTPUT_NAME);
        newToOldContactMap.put(outputContact, rootOutputContact);

        List<BooleanFormula> leafSetFunctions = new LinkedList<>();
        for (Contact rootInputContact: rootInputContacts) {
            BooleanFormula leafSetFunction = null;
            for (FunctionComponent leafComponent: leafComponents) {
                FunctionContact leafOutputContact = getOutputContact(circuit, leafComponent);
                List<Contact> leafInputContacts = new LinkedList<>(leafComponent.getInputs());

                Set<MathNode> oldContacts = circuit.getPostset(leafOutputContact);
                if (oldContacts.contains(rootInputContact)) {
                    List<BooleanFormula> replacementContacts = new LinkedList<>();
                    for (Contact leafInputContact: leafInputContacts) {
                        FunctionContact inputContact = new FunctionContact(IOType.INPUT);
                        component.add(inputContact);
                        circuit.setName(inputContact, rootInputContact.getName() + leafInputContact.getName());
                        replacementContacts.add(inputContact);
                        newToOldContactMap.put(inputContact, leafInputContact);
                    }
                    leafSetFunction = BooleanUtils.replaceDumb(
                            leafOutputContact.getSetFunction(), leafInputContacts, replacementContacts);
                }

            }
            if (leafSetFunction == null) {
                FunctionContact inputContact = new FunctionContact(IOType.INPUT);
                component.add(inputContact);
                circuit.setName(inputContact, rootInputContact.getName());
                newToOldContactMap.put(inputContact, rootInputContact);
                leafSetFunction = inputContact;
            }
            leafSetFunctions.add(leafSetFunction);
        }
        BooleanFormula rootSetFunction = rootOutputContact.getSetFunction();
        BooleanFormula setFunction = printFunctionSubstitution(rootSetFunction, rootInputContacts, leafSetFunctions);
        outputContact.setSetFunctionQuiet(setFunction);

        connectMergedComponent(circuit, component, rootComponent, newToOldContactMap);
        return component;
    }

    private BooleanFormula printFunctionSubstitution(BooleanFormula function, List<Contact> inputContacts, List<BooleanFormula> inputFunctions) {
        final BooleanFormula setFunction = BooleanUtils.replaceDumb(function, inputContacts, inputFunctions);
        if (CommonDebugSettings.getVerboseImport()) {
            LogUtils.logInfo("Expression substitution");
            LogUtils.logInfo("  Original: " + StringGenerator.toString(function));
            Iterator<Contact> contactIterator = inputContacts.iterator();
            Iterator<BooleanFormula> formulaIterator = inputFunctions.iterator();
            while (contactIterator.hasNext() && formulaIterator.hasNext()) {
                Contact contact = contactIterator.next();
                BooleanFormula formula = formulaIterator.next();
                LogUtils.logInfo("  Replacement: " + contact.getName() + " = " + StringGenerator.toString(formula));
            }
            LogUtils.logInfo("  Result: " + StringGenerator.toString(setFunction));
        }
        return setFunction;
    }

    private FunctionContact getOutputContact(Circuit circuit, FunctionComponent component) {
        Collection<Contact> outputContacts = component.getOutputs();
        if (outputContacts.size() != 1) {
            throw new RuntimeException("Cannot determin the output of component '" + circuit.getName(component) + "'.");
        }
        return (FunctionContact) outputContacts.iterator().next();
    }

    private void connectMergedComponent(Circuit circuit, FunctionComponent newComponent,
            FunctionComponent oldComponent, HashMap<Contact, Contact> newToOldContactMap) {

        FunctionContact oldOutputContact = getOutputContact(circuit, oldComponent);
        FunctionContact newOutputContact = getOutputContact(circuit, newComponent);
        for (MathConnection oldConnection: new HashSet<>(circuit.getConnections(oldOutputContact))) {
            if (oldConnection.getFirst() != oldOutputContact) continue;
            MathNode toNode = oldConnection.getSecond();
            circuit.remove(oldConnection);
            try {
                boolean hasNewContact = false;
                for (Contact newContact: newToOldContactMap.keySet()) {
                    Contact oldContact = newToOldContactMap.get(newContact);
                    if (toNode == oldContact) {
                        circuit.connect(newOutputContact, newContact);
                        hasNewContact = true;
                    }
                }
                if (!hasNewContact) {
                    circuit.connect(newOutputContact, toNode);
                }
            } catch (InvalidConnectionException e) {
            }
        }
        for (Contact newContact: newToOldContactMap.keySet()) {
            if (newContact.isOutput()) continue;
            Contact oldContact = newToOldContactMap.get(newContact);
            if (oldContact == null) continue;
            for (MathNode fromNode: circuit.getPreset(oldContact)) {
                try {
                    circuit.connect(fromNode, newContact);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
