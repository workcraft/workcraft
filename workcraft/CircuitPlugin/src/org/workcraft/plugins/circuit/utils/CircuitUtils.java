package org.workcraft.plugins.circuit.utils;

import org.workcraft.Framework;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CircuitUtils {

    public static BooleanFormula getDriverFormula(Circuit circuit, BooleanFormula formula) {
        if (formula == null) {
            return null;
        }
        Map<String, BooleanVariable> variableMap = new HashMap<>();
        List<BooleanVariable> variables = new ArrayList<>();
        List<BooleanFormula> values = new ArrayList<>();
        for (BooleanVariable variable : FormulaUtils.extractOrderedVariables(formula)) {
            if (variable instanceof Contact) {
                Pair<Contact, Boolean> pair = CircuitUtils.findDriverAndInversionSkipZeroDelay(circuit, (Contact) variable);
                if (pair != null) {
                    variables.add(variable);
                    String signalName = getSignalReference(circuit, pair.getFirst());
                    BooleanVariable replacement = variableMap.computeIfAbsent(signalName, FreeVariable::new);
                    values.add(pair.getSecond() ? new Not(replacement) : replacement);
                }
            }
        }
        return FormulaUtils.replace(formula, variables, values);
    }

    public static Pair<Contact, Boolean> findDriverAndInversionSkipZeroDelay(Circuit circuit, Contact contact) {
        Pair<Contact, Boolean> result = null;
        Contact driver = CircuitUtils.findDriver(circuit, contact, false);
        boolean inversion = false;
        if (driver != null) {
            Node parent = driver.getParent();
            if (parent instanceof FunctionComponent) {
                FunctionComponent component = (FunctionComponent) parent;
                if (component.getIsZeroDelay() && (component.isInverter() || component.isBuffer())) {
                    Contact input = component.getFirstInput();
                    driver = CircuitUtils.findDriver(circuit, input, false);
                    inversion = component.isInverter();
                }
            }
            result = Pair.of(driver, inversion);
        }
        return result;
    }

    public static VisualContact findDriver(VisualCircuit circuit, VisualContact contact, boolean transparentZeroDelayComponents) {
        Contact mathDriver = findDriver(circuit.getMathModel(), contact.getReferencedComponent(), transparentZeroDelayComponents);
        return circuit.getVisualComponent(mathDriver, VisualContact.class);
    }

    public static Contact findDriver(Circuit circuit, MathNode curNode, boolean transparentZeroDelayComponents) {
        Contact result = null;
        HashSet<MathNode> visited = new HashSet<>();
        Queue<MathNode> queue = new LinkedList<>();
        if (curNode instanceof MathConnection) {
            queue.add(((MathConnection) curNode).getFirst());
        } else {
            queue.add(curNode);
        }
        while (!queue.isEmpty()) {
            if (queue.size() != 1) {
                throw new RuntimeException("Found more than one potential driver for '"
                        + circuit.getNodeReference(curNode) + "'!");
            }
            MathNode node = queue.remove();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node instanceof Joint) {
                queue.addAll(circuit.getPreset(node));
            } else if (node instanceof Contact) {
                Contact contact = (Contact) node;
                // Support for zero delay buffers and inverters.
                Contact zeroDelayInput = transparentZeroDelayComponents ? findZeroDelayInput(contact) : null;
                if (zeroDelayInput != null) {
                    queue.addAll(circuit.getPreset(zeroDelayInput));
                } else if (contact.isDriver()) {
                    result = contact;
                } else {
                    // Is it necessary to check that (node == curNode) before adding preset to queue?
                    queue.addAll(circuit.getPreset(contact));
                }
            } else {
                throw new RuntimeException("Unexpected node '" + circuit.getNodeReference(node)
                + "' in the driver trace for node '" + circuit.getNodeReference(curNode) + "'!");
            }
        }
        return result;
    }

    private static Contact findZeroDelayInput(Contact contact) {
        Contact zeroDelayInput = null;
        Node parent = contact.getParent();
        if (contact.isOutput() && (parent instanceof FunctionComponent)) {
            FunctionComponent component = (FunctionComponent) parent;
            if (component.getIsZeroDelay() && (component.isBuffer() || component.isInverter())) {
                zeroDelayInput = component.getFirstInput();
            }
        }
        return zeroDelayInput;
    }

    public static Set<VisualContact> findDriven(VisualCircuit circuit, VisualContact contact) {
        return findDriven(circuit, contact, true);
    }

    public static Set<VisualContact> findDriven(VisualCircuit circuit, VisualContact contact, boolean transparentZeroDelayComponents) {
        Collection<Contact> drivenContacts = findDriven(circuit.getMathModel(), contact.getReferencedComponent(), transparentZeroDelayComponents);
        return getVisualContacts(circuit, drivenContacts);
    }

    public static Collection<Contact> findDriven(Circuit circuit, MathNode curNode, boolean transparentZeroDelayComponents) {
        Set<Contact> result = new HashSet<>();
        HashSet<MathNode> visited = new HashSet<>();
        Queue<MathNode> queue = new LinkedList<>();
        if (curNode instanceof MathConnection) {
            queue.add(((MathConnection) curNode).getSecond());
        } else {
            queue.add(curNode);
        }
        while (!queue.isEmpty()) {
            MathNode node = queue.remove();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node instanceof Joint) {
                queue.addAll(circuit.getPostset(node));
            } else if (node instanceof Contact) {
                Contact contact = (Contact) node;
                // Support for zero delay buffers and inverters.
                Contact zeroDelayOutput = transparentZeroDelayComponents ? findZeroDelayOutput(contact) : null;
                if (zeroDelayOutput != null) {
                    queue.addAll(circuit.getPostset(zeroDelayOutput));
                } else if (contact.isDriven()) {
                    result.add(contact);
                } else {
                    // Is it necessary to check that (node == curNode) before adding postset to queue?
                    queue.addAll(circuit.getPostset(contact));
                }
            } else {
                throw new RuntimeException("Unexpected node '" + circuit.getNodeReference(node)
                + "' in the driven trace for node '" + circuit.getNodeReference(curNode) + "'!");
            }
        }
        return result;
    }

    private static Contact findZeroDelayOutput(Contact contact) {
        Contact zeroDelayOutput = null;
        Node parent = contact.getParent();
        if (contact.isInput() && (parent instanceof FunctionComponent)) {
            FunctionComponent component = (FunctionComponent) parent;
            if (component.getIsZeroDelay() && (component.isBuffer() || component.isInverter())) {
                zeroDelayOutput = component.getFirstOutput();
            }
        }
        return zeroDelayOutput;
    }

    public static Contact findSignal(Circuit circuit, Contact contact, boolean transparentZeroDelayComponents) {
        Contact result = contact;
        Contact driver = findDriver(circuit, contact, transparentZeroDelayComponents);
        if (driver != null) {
            result = driver;
            for (Contact signal: Hierarchy.getDescendantsOfType(circuit.getRoot(), Contact.class)) {
                if (signal.isPort() && signal.isOutput()) {
                    if (driver == CircuitUtils.findDriver(circuit, signal, transparentZeroDelayComponents)) {
                        signal.setInitToOne(driver.getInitToOne());
                        result = signal;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static VisualContact findSignal(VisualCircuit circuit, VisualContact contact, boolean transparentZeroDelayComponents) {
        Contact mathSignal = findSignal(circuit.getMathModel(), contact.getReferencedComponent(), transparentZeroDelayComponents);
        return circuit.getVisualComponent(mathSignal, VisualContact.class);
    }

    public static String getSignalReference(Circuit circuit, Contact contact) {
        String result = null;
        if (contact.isPort() || contact.isInput()) {
            result = getContactReference(circuit, contact);
        } else {
            result = getOutputContactReference(circuit, contact);
        }
        return result;
    }

    public static String getSignalReference(VisualCircuit circuit, VisualContact contact) {
        return getSignalReference(circuit.getMathModel(), contact.getReferencedComponent());
    }

    public static String getContactReference(Circuit circuit, Contact contact) {
        return circuit.getNodeReference(contact);
    }

    private static String getOutputContactReference(Circuit circuit, Contact contact) {
        String result = null;
        Node parent = contact.getParent();
        if (parent instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) parent;

            Contact outputPort = getDrivenOutputPort(circuit, contact);
            if (outputPort != null) {
                // If a single output port is driven, then take its name.
                result = circuit.getNodeReference(outputPort);
            } else {
                // If the component has a single output, use the component name. Otherwise append the contact.
                if (component.getOutputs().size() == 1) {
                    result = Identifier.truncateNamespaceSeparator(circuit.getNodeReference(component));
                } else {
                    result = circuit.getNodeReference(contact);
                }
            }
        }
        return result;
    }

    public static Contact getDrivenOutputPort(Circuit circuit, Contact contact) {
        Contact result = null;
        boolean multipleOutputPorts = false;
        for (Contact vc: findDriven(circuit, contact, true)) {
            if (vc.isPort() && vc.isOutput()) {
                if (result != null) {
                    multipleOutputPorts = true;
                }
                result = vc;
            }
        }
        if (multipleOutputPorts) {
            result = null;
        }
        return result;
    }

    public static Signal.Type getSignalType(VisualCircuit circuit, VisualContact contact) {
        return getSignalType(circuit.getMathModel(), contact.getReferencedComponent());
    }

    public static Signal.Type getSignalType(Circuit circuit, Contact contact) {
        Signal.Type result = Signal.Type.INTERNAL;
        if (contact.isPort()) {
            // Primary port
            if (contact.isInput()) {
                result = Signal.Type.INPUT;
            } else if (contact.isOutput()) {
                result = Signal.Type.OUTPUT;
            }
        } else {
            CircuitComponent component = (CircuitComponent) contact.getParent();
            if (component.getIsEnvironment()) {
                // Contact of an environment component
                if (contact.isInput()) {
                    result = Signal.Type.OUTPUT;
                } else if (contact.isOutput()) {
                    result = Signal.Type.INPUT;
                }
            } else {
                // Contact of an ordinary component
                if (contact.isOutput() && (getDrivenOutputPort(circuit, contact) != null)) {
                    result = Signal.Type.OUTPUT;
                }
            }
        }
        return result;
    }

    public static BooleanFormula parseContactFunction(VisualCircuit circuit, VisualFunctionContact contact, String function)
            throws ParseException {

        Node parent = contact.getParent();
        if (parent instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) parent;
            return parsePinFuncton(circuit, component, function);
        } else {
            return parsePortFuncton(circuit, function);
        }
    }

    public static BooleanFormula parsePinFuncton(VisualCircuit circuit, VisualFunctionComponent component, String function) throws ParseException {
        if ((function == null) || function.isEmpty()) {
            return null;
        }
        return BooleanFormulaParser.parse(function, name -> {
            BooleanFormula result = null;
            VisualFunctionContact contact = circuit.getOrCreateContact(component, name, IOType.INPUT);
            if ((contact != null) && (contact.getReferencedComponent() instanceof BooleanFormula)) {
                result = contact.getReferencedComponent();
            }
            return result;
        });
    }

    public static BooleanFormula parsePortFuncton(VisualCircuit circuit, String function) throws ParseException {
        if ((function == null) || function.isEmpty()) {
            return null;
        }
        return BooleanFormulaParser.parse(function, name -> {
            BooleanFormula result = null;
            VisualFunctionContact port = circuit.getOrCreatePort(name, IOType.OUTPUT);
            if ((port != null) && (port.getReferencedComponent() instanceof BooleanFormula)) {
                result = port.getReferencedComponent();
            }
            return result;
        });
    }

    public static BooleanFormula parsePinFuncton(Circuit circuit, FunctionComponent component, String function) throws ParseException {
        if ((function == null) || function.isEmpty()) {
            return null;
        }
        return BooleanFormulaParser.parse(function, name -> {
            FunctionContact contact = (FunctionContact) circuit.getNodeByReference(component, name);
            if (contact == null) {
                contact = new FunctionContact();
                contact.setIOType(IOType.INPUT);
                component.add(contact);
                circuit.setName(contact, name);
            }
            return contact;
        });
    }

    private static Set<VisualContact> getVisualContacts(final VisualCircuit visualCircuit, Collection<Contact> mathContacts) {
        Set<VisualContact> result = new HashSet<>();
        for (Contact mathContact: mathContacts) {
            VisualContact visualContact = visualCircuit.getVisualComponent(mathContact, VisualContact.class);
            if (visualContact != null) {
                result.add(visualContact);
            }
        }
        return result;
    }

    public static VisualJoint detachJoint(VisualCircuit circuit, VisualContact driver) {
        Set<VisualConnection> connections = new HashSet<>(circuit.getConnections(driver));
        if (!driver.isDriver() || (connections.size() <= 1)) {
            return null;
        }

        Container container = (Container) driver.getParent();
        if (container instanceof VisualCircuitComponent) {
            container = (Container) container.getParent();
        }
        VisualJoint joint = circuit.createJoint(container);
        joint.setRootSpacePosition(driver.getRootSpacePosition());

        try {
            circuit.connect(driver, joint);
        } catch (InvalidConnectionException e) {
            LogUtils.logWarning(e.getMessage());
        }

        for (VisualConnection connection: connections) {
            if (connection instanceof VisualCircuitConnection) {
                circuit.remove(connection);
                try {
                    VisualNode driven = connection.getSecond();
                    VisualConnection newConnection = circuit.connect(joint, driven);
                    newConnection.copyShape(connection);
                    newConnection.copyStyle(connection);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarning(e.getMessage());
                }
            }
        }
        return joint;
    }

    public static boolean isSelfLoop(Connection connection) {
        Node firstParent = connection.getFirst().getParent();
        Node secondParent = connection.getSecond().getParent();
        return ((firstParent instanceof VisualCircuitComponent) || (firstParent instanceof CircuitComponent))
                && ((secondParent instanceof VisualCircuitComponent) || (secondParent instanceof CircuitComponent))
                && (firstParent == secondParent);
    }

    public static String gateToString(VisualCircuit circuit, VisualFunctionComponent gate) {
        String result = circuit.getMathReference(gate);

        VisualFunctionContact outputContact = gate.getGateOutput();
        String outputName = outputContact.getName();

        BooleanFormula setFunction = outputContact.getSetFunction();
        String setString = StringGenerator.toString(setFunction);

        BooleanFormula resetFunction = outputContact.getResetFunction();
        String resetString = StringGenerator.toString(resetFunction);

        if (!setString.isEmpty() && resetString.isEmpty()) {
            result += " [" + outputName + " = " + setString + "]";
        } else if (setString.isEmpty() && !resetString.isEmpty()) {
            result += " [" + outputName + "\u2193 = " + resetString + "]";
        } else if (!setString.isEmpty() && !resetString.isEmpty()) {
            result += " [" + outputName + "\u2191 = " + setString + "; " + outputName + "\u2193 = " + resetString + "]";
        }
        return result;
    }

    public static Set<FunctionContact> getBubbleContacts(FunctionComponent component) {
        Set<FunctionContact> result = new HashSet<>();
        for (FunctionContact outputContact: component.getFunctionOutputs()) {
            BooleanFormula setFunction = outputContact.getSetFunction();
            BooleanFormula resetFunction = outputContact.getResetFunction();
            if ((setFunction == null) || (resetFunction != null)) continue;
            if (setFunction instanceof Not) {
                result.add(outputContact);
            }
            Set<BooleanVariable> bubbleLiterals = FormulaUtils.extractNegatedVariables(setFunction);
            for (BooleanVariable literal: bubbleLiterals) {
                if (literal instanceof FunctionContact) {
                    FunctionContact inputContact = (FunctionContact) literal;
                    result.add(inputContact);
                }
            }
        }
        return result;
    }

    public static boolean mapUnmappedBuffers(WorkspaceEntry we) {
        boolean result = false;
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        FreeVariable inVar = new FreeVariable("I");
        Pair<Gate, Map<BooleanVariable, String>> mapping = GenlibUtils.findMapping(inVar, LibraryManager.getLibrary());
        if (mapping != null) {
            Gate buf = mapping.getFirst();
            Map<BooleanVariable, String> assignments = mapping.getSecond();
            String inName = assignments.get(inVar);
            String outName = buf.function.name;
            for (FunctionComponent component : circuit.getFunctionComponents()) {
                if (!component.isMapped() && component.isBuffer()) {
                    Contact input = component.getFirstInput();
                    Contact output = component.getFirstOutput();
                    component.setModule(buf.name);
                    circuit.setName(input, inName);
                    circuit.setName(output, outName);
                    result = true;
                }
            }
        }
        return result;
    }

    public static boolean checkUnmappedSignals(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        List<String> signals = new ArrayList<>();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            if (!component.isMapped()) {
                signals.add(circuit.getNodeReference(component));
            }
        }
        if (!signals.isEmpty()) {
            String msg = TextUtils.wrapMessageWithItems("Technology mapping failed to implement signal", signals);
            DialogUtils.showWarning(msg);
            return false;
        }
        return true;
    }

    public static void removeUnusedPins(VisualCircuit circuit, VisualFunctionComponent component) {
        Set<BooleanVariable> usedVariables = GateUtils.getUsedVariables(component.getReferencedComponent());
        for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
            if (!usedVariables.contains(contact.getReferencedComponent()) &&
                    ((contact.isDriver() && circuit.getPostset(contact).isEmpty()) ||
                    (contact.isDriven() && circuit.getPreset(contact).isEmpty()))) {

                component.remove(contact);
            }
        }
    }

    public static VisualFunctionContact getOrCreatePort(VisualCircuit circuit, String portName,
            IOType ioType, VisualContact.Direction direction) {

        VisualFunctionContact result = null;
        VisualComponent component = circuit.getVisualComponentByMathReference(portName, VisualComponent.class);
        if (component == null) {
            result = circuit.getOrCreatePort(portName, ioType);
            if (result == null) {
                DialogUtils.showError("Cannot create port '" + portName + "'.");
                return null;
            }
        } else if (component instanceof VisualFunctionContact) {
            result = (VisualFunctionContact) component;
            if (result.getReferencedComponent().getIOType() != ioType) {
                DialogUtils.showError("Cannot reuse existing port '" + portName + "' because it is of different type.");
                return null;
            }
            LogUtils.logWarning("Reusing existing port '" + portName + "'.");
        } else {
            DialogUtils.showError("Cannot insert port '" + portName + "' because a component with the same name already exists.");
            return null;
        }
        result.setDirection(direction);
        return result;
    }

    public static VisualFunctionContact getOrCreateContact(VisualCircuit circuit, VisualFunctionComponent component,
            String contactName, Contact.IOType ioType, VisualContact.Direction direction) {

        VisualFunctionContact result = getFunctionContact(circuit, component, contactName);
        if (result == null) {
            result = circuit.getOrCreateContact(component, contactName, ioType);
            component.setPositionByDirection(result, direction, false);
        }
        return result;
    }

    public static VisualFunctionContact getFunctionContact(VisualCircuit circuit,
            VisualFunctionComponent component, String contactName) {

        String ref = NamespaceHelper.getReference(circuit.getMathReference(component), contactName);
        return circuit.getVisualComponentByMathReference(ref, VisualFunctionContact.class);
    }

    public static void disconnectContact(VisualCircuit circuit, VisualContact contact) {
        for (VisualConnection connection : circuit.getConnections(contact)) {
            circuit.remove(connection);
        }
    }

    public static Collection<FunctionComponent> correctZeroDelayInitialState(Circuit circuit) {
        Collection<FunctionComponent> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getIsZeroDelay()) {
                FunctionContact output = component.getGateOutput();
                boolean initToOne = output.getInitToOne();
                GateUtils.propagateInitialState(circuit, component);
                if (initToOne != output.getInitToOne()) {
                    result.add(component);
                }
            }
        }
        if (!result.isEmpty()) {
            LogUtils.logInfo("Corrected initial state of " + result.size() + " zero delay component(s)");
        }
        return result;
    }

    public static Set<String> getInputPortNames(Circuit circuit) {
        Set<String> result = new HashSet<>();
        for (Contact port : circuit.getInputPorts()) {
            result.add(circuit.getName(port));
        }
        return result;
    }

    public static Set<String> getOutputPortNames(Circuit circuit) {
        Set<String> result = new HashSet<>();
        for (Contact port : circuit.getOutputPorts()) {
            result.add(circuit.getName(port));
        }
        return result;
    }

    public static Set<String> getInputPinNames(CircuitComponent component) {
        return component.getInputs().stream().map(Contact::getName).collect(Collectors.toSet());
    }

    public static Set<String> getOutputPinNames(CircuitComponent component) {
        return component.getOutputs().stream().map(Contact::getName).collect(Collectors.toSet());
    }

    public static void setTitleAndEnvironment(VisualCircuit visualCircuit, WorkspaceEntry stgWe) {
        visualCircuit.setTitle(stgWe.getModelTitle());
        Workspace workspace = Framework.getInstance().getWorkspace();
        File stgFile = workspace.getFile(stgWe);
        if ((stgFile == null) || !stgFile.exists()) {
            DialogUtils.showError("Unsaved STG cannot be set as the circuit environment.");
        } else {
            visualCircuit.getMathModel().setEnvironmentFile(stgFile);
            if (stgWe.isChanged()) {
                DialogUtils.showWarning("The STG with unsaved changes is set as the circuit environment.");
            }
        }
    }

    public static boolean hasContacts(VisualCircuit circuit, VisualCircuitComponent component, Collection<String> contactNames) {
        return hasContacts(circuit.getMathModel(), component.getReferencedComponent(), contactNames);
    }

    public static boolean hasContacts(Circuit circuit, CircuitComponent component, Collection<String> contactNames) {
        return contactNames.stream()
                .allMatch(contactName -> circuit.getNodeByReference(component, contactName) != null);
    }

}
