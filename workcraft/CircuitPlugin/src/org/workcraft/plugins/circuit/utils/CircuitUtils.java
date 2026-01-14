package org.workcraft.plugins.circuit.utils;

import org.workcraft.Framework;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
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

import java.awt.geom.Point2D;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public final class CircuitUtils {

    private static final String DOWN_ARROW_SYMBOL = Character.toString((char) 0x2193);
    private static final String UP_ARROW_SYMBOL = Character.toString((char) 0x2191);

    private CircuitUtils() {
    }

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
        Contact driver = CircuitUtils.findDriver(circuit, contact, false);
        if (driver == null) {
            return null;
        }
        Node parent = driver.getParent();
        if (parent instanceof FunctionComponent component) {
            if (component.getIsZeroDelay() && (component.isInverter() || component.isBuffer())) {
                Contact input = component.getFirstInput();
                Contact zeroDelayDriver = CircuitUtils.findDriver(circuit, input, false);
                if (zeroDelayDriver != null) {
                    return Pair.of(zeroDelayDriver, component.isInverter());
                }
            }
        }
        return Pair.of(driver, false);
    }

    public static boolean findInitToOneFromDriver(VisualCircuit circuit, VisualContact contact) {
        return findInitToOneFromDriver(circuit.getMathModel(), contact.getReferencedComponent());
    }

    public static boolean findInitToOneFromDriver(Circuit circuit, Contact contact) {
        Pair<Contact, Boolean> pair = CircuitUtils.findDriverAndInversionSkipZeroDelay(circuit, contact);
        return (pair == null) ? contact.getInitToOne() : (pair.getFirst().getInitToOne() != pair.getSecond());
    }

    public static VisualContact findDriver(VisualCircuit circuit, VisualContact contact, boolean transparentZeroDelayComponents) {
        Contact mathDriver = findDriver(circuit.getMathModel(), contact.getReferencedComponent(), transparentZeroDelayComponents);
        return circuit.getVisualComponent(mathDriver, VisualContact.class);
    }

    public static Contact findDriver(Circuit circuit, MathNode curNode, boolean transparentZeroDelayComponents) {
        Contact result = null;
        HashSet<MathNode> visited = new HashSet<>();
        Queue<MathNode> queue = new ArrayDeque<>();
        if (curNode instanceof MathConnection) {
            queue.add(((MathConnection) curNode).getFirst());
        } else if (curNode != null) {
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
            } else if (node instanceof Contact contact) {
                if (contact.isDriven()) {
                    // Is it necessary to check that (node == curNode) before adding preset to queue?
                    queue.addAll(circuit.getPreset(contact));
                } else {
                    result = contact;
                    // Support for zero delay buffers and inverters.
                    if (contact.isZeroDelayPin() && transparentZeroDelayComponents) {
                        Contact zeroDelayInput = findZeroDelayInput(contact);
                        if (zeroDelayInput != null) {
                            queue.addAll(circuit.getPreset(zeroDelayInput));
                        }
                    }
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
        if (contact.isOutput() && (parent instanceof FunctionComponent component)) {
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

    public static Set<Contact> findDriven(Circuit circuit, MathNode curNode, boolean transparentZeroDelayComponents) {
        Set<Contact> result = new HashSet<>();
        HashSet<MathNode> visited = new HashSet<>();
        Queue<MathNode> queue = new ArrayDeque<>();
        if (curNode instanceof MathConnection) {
            queue.add(((MathConnection) curNode).getSecond());
        } else if (curNode != null) {
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
            } else if (node instanceof Contact contact) {
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

    public static Pair<Integer, Boolean> calcFanout(VisualCircuit circuit, VisualContact contact) {
        return calcFanout(circuit.getMathModel(), contact.getReferencedComponent());
    }

    public static Pair<Integer, Boolean> calcFanout(Circuit circuit, MathNode curNode) {
        int fanoutCount = 0;
        boolean isPortDriver = false;
        Queue<MathNode> queue = new ArrayDeque<>();
        if (curNode instanceof MathConnection) {
            queue.add(((MathConnection) curNode).getSecond());
        } else if (curNode != null) {
            queue.addAll(circuit.getPostset(curNode));
        }
        while (!queue.isEmpty()) {
            MathNode node = queue.remove();
            if (node instanceof Joint) {
                queue.addAll(circuit.getPostset(node));
            } else if (node instanceof Contact contact) {
                if (contact.isDriven()) {
                    fanoutCount++;
                }
                if (contact.isPort()) {
                    isPortDriver = true;
                }
            } else {
                throw new RuntimeException("Unexpected node '" + circuit.getNodeReference(node)
                        + "' in the driven trace for node '" + circuit.getNodeReference(curNode) + "'!");
            }
        }
        return Pair.of(fanoutCount, isPortDriver);
    }

    public static MathNode getReferencedMathNode(VisualNode visualNode) {
        if (visualNode instanceof VisualContact) {
            return ((VisualContact) visualNode).getReferencedComponent();
        }
        if (visualNode instanceof VisualReplicaContact) {
            return ((VisualReplicaContact) visualNode).getReferencedComponent();
        }
        if (visualNode instanceof VisualJoint) {
            return ((VisualJoint) visualNode).getReferencedComponent();
        }
        if (visualNode instanceof VisualConnection) {
            return ((VisualConnection) visualNode).getReferencedConnection();
        }
        if (visualNode instanceof VisualCircuitComponent) {
            return ((VisualCircuitComponent) visualNode).getReferencedComponent();
        }
        return null;
    }

    private static Contact findZeroDelayOutput(Contact contact) {
        Contact zeroDelayOutput = null;
        Node parent = contact.getParent();
        if (contact.isInput() && (parent instanceof FunctionComponent component)) {
            if (component.getIsZeroDelay() && (component.isBuffer() || component.isInverter())) {
                zeroDelayOutput = component.getFirstOutput();
            }
        }
        return zeroDelayOutput;
    }

    public static Contact findSignal(Circuit circuit, Contact contact, boolean transparentZeroDelayComponents) {
        Contact driver = findDriver(circuit, contact, transparentZeroDelayComponents);
        if (driver == null) {
            return contact;
        }
        for (Contact signal : circuit.getOutputPorts()) {
            if (driver == CircuitUtils.findDriver(circuit, signal, transparentZeroDelayComponents)) {
                // FIXME: Set initial state of output port from driver, so signal.getInitToOne() returns correct state
                signal.setInitToOne(driver.getInitToOne());
                return signal;
            }
        }
        return driver;
    }

    public static String getSignalReference(Circuit circuit, Contact contact) {
        return contact.isPort() || contact.isInput()
                ? getContactReference(circuit, contact)
                : getOutputContactReference(circuit, contact);
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
        if (parent instanceof FunctionComponent component) {

            Contact outputPort = getDrivenOutputPort(circuit, contact);
            if (outputPort != null) {
                // If a single output port is driven, then take its name.
                result = circuit.getNodeReference(outputPort);
            } else {
                // If the component has a single output, use the component name. Otherwise, append the contact.
                if (component.getOutputs().size() == 1) {
                    result = circuit.getComponentReference(component);
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
        for (Contact driven : findDriven(circuit, contact, true)) {
            if (driven.isPort() && driven.isOutput()) {
                if (result != null) {
                    multipleOutputPorts = true;
                }
                result = driven;
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

    public static BooleanFormula parseContactFunction(VisualCircuit circuit, VisualFunctionContact contact,
            String function) throws ParseException {

        Node parent = contact.getParent();
        if (parent instanceof VisualFunctionComponent component) {
            return parsePinFunction(circuit, component, function);
        } else {
            return parsePortFunction(circuit, function);
        }
    }

    public static BooleanFormula parsePinFunction(VisualCircuit circuit, VisualFunctionComponent component,
            String function) throws ParseException {

        return BooleanFormulaParser.parse(function, name -> circuit
                .getExistingPinOrCreateInputPin(component, name)
                .getReferencedComponent());
    }

    public static BooleanFormula parsePortFunction(VisualCircuit circuit, String function) throws ParseException {
        return BooleanFormulaParser.parse(function, name -> circuit
                .getOrCreatePort(name, IOType.OUTPUT)
                .getReferencedComponent());
    }

    public static BooleanFormula parsePinFunction(Circuit circuit, FunctionComponent component, String function)
            throws ParseException {

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

    public static VisualJoint detachJoint(VisualCircuit circuit, VisualContact driver, double offset) {
        VisualJoint joint = detachJoint(circuit, driver);
        if (joint != null) {
            Point2D jointPos = SpaceUtils.getOffsetContactPosition(driver, offset);
            joint.setRootSpacePosition(jointPos);
            for (VisualConnection connection : circuit.getConnections(joint)) {
                if ((connection.getFirst() == joint) && (connection.getGraphic() instanceof Polyline polyline)) {
                    ControlPoint firstControlPoint = polyline.getFirstControlPoint();
                    if ((firstControlPoint != null) && Geometry.isNegligible(jointPos.distance(firstControlPoint.getPosition()))) {
                        polyline.remove(firstControlPoint);
                    }
                }
            }
        }
        return joint;
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
                && (firstParent == secondParent);
    }

    public static String cellToString(VisualCircuit circuit, VisualFunctionComponent cell) {
        return cellToString(circuit.getMathModel(), cell.getReferencedComponent());
    }

    public static String cellToString(Circuit circuit, FunctionComponent cell) {
        StringBuilder outputDetails = new StringBuilder();
        boolean isFirstOutputPin = true;
        for (FunctionContact outputPin : cell.getFunctionOutputs()) {
            String outputName = outputPin.getName();
            BooleanFormula setFunction = outputPin.getSetFunction();
            BooleanFormula resetFunction = outputPin.getResetFunction();

            String outputDetail = getOutputFunctionString(setFunction, resetFunction, outputName);
            if (outputDetail != null) {
                if (!isFirstOutputPin) {
                    outputDetails.append("; ");
                }
                outputDetails.append(outputDetail);
                isFirstOutputPin = false;
            }
        }
        String result = circuit.getComponentReference(cell);
        if (!outputDetails.isEmpty()) {
            result += " [" + outputDetails + "]";
        }
        return result;
    }

    public static String getOutputFunctionString(BooleanFormula setFunction, BooleanFormula resetFunction,
            String outputName) {

        String result = null;
        if (outputName == null) {
            outputName = "";
        }
        String setString = StringGenerator.toString(setFunction);
        String resetString = StringGenerator.toString(resetFunction);
        if (!setString.isEmpty() && resetString.isEmpty()) {
            if (outputName.isEmpty()) {
                result = setString;
            } else {
                result = outputName + " = " + setString;
            }
        } else if (setString.isEmpty() && !resetString.isEmpty()) {
            if (outputName.isEmpty()) {
                result = DOWN_ARROW_SYMBOL + ": " + resetString;
            } else {
                result = outputName + DOWN_ARROW_SYMBOL + " = " + resetString;
            }
        } else if (!setString.isEmpty()) {
            if (outputName.isEmpty()) {
                result = UP_ARROW_SYMBOL + ": " + setString + ", " + DOWN_ARROW_SYMBOL + ": " + resetString;
            } else {
                result = outputName + UP_ARROW_SYMBOL + " = " + setString + ", "
                        + outputName + DOWN_ARROW_SYMBOL + " = " + resetString;
            }
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
                if (literal instanceof FunctionContact inputContact) {
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
        VisualComponent component = circuit.getVisualComponentByMathReference(portName);
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

    public static boolean correctInitialState(Circuit circuit) {
        Collection<FunctionComponent> changedComponents = correctZeroDelayInitialState(circuit);
        if (!changedComponents.isEmpty()) {
            LogUtils.logInfo("Corrected initial state of " + changedComponents.size() + " zero delay component(s)");
        }
        Collection<Contact> changedContacts = correctOutputPortInitialState(circuit);
        if (!changedContacts.isEmpty()) {
            LogUtils.logInfo("Corrected initial state of " + changedContacts.size() + " output ports(s)");
        }
        return !changedComponents.isEmpty() || !changedContacts.isEmpty();
    }

    private static Collection<FunctionComponent> correctZeroDelayInitialState(Circuit circuit) {
        Collection<FunctionComponent> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getIsZeroDelay()) {
                FunctionContact output = component.getGateOutput();
                boolean initToOne = output.getInitToOne();
                GateUtils.propagateInitialState(circuit, component, Collections.singleton(output));
                if (initToOne != output.getInitToOne()) {
                    result.add(component);
                }
            }
        }
        return result;
    }

    private static Collection<Contact> correctOutputPortInitialState(Circuit circuit) {
        Collection<Contact> result = new HashSet<>();
        for (Contact contact : circuit.getOutputPorts()) {
            boolean state = CircuitUtils.findInitToOneFromDriver(circuit, contact);
            if (state != contact.getInitToOne()) {
                contact.setInitToOne(state);
                result.add(contact);
            }
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

    public static boolean isConstantDriver(FunctionContact contact) {
        return isConstantDriver1(contact) || isConstantDriver0(contact);
    }

    public static boolean isConstantDriver0(FunctionContact contact) {
        return contact.isDriver() && !contact.getInitToOne() && cannotRise(contact);
    }

    public static boolean cannotRise(FunctionContact contact) {
        BooleanFormula setFunction = contact.getSetFunction();
        if (setFunction != null) {
            return isConstant0(setFunction);
        }
        BooleanFormula resetFunction = contact.getResetFunction();
        return isConstant1(resetFunction);
    }

    public static boolean isConstantDriver1(FunctionContact contact) {
        return contact.isDriver() && contact.getInitToOne() && cannotFall(contact);
    }

    public static boolean cannotFall(FunctionContact contact) {
        BooleanFormula resetFunction = contact.getResetFunction();
        if (resetFunction != null) {
            return isConstant0(resetFunction);
        }
        BooleanFormula setFunction = contact.getSetFunction();
        return isConstant1(setFunction);
    }

    public static boolean isConstant(BooleanFormula formula) {
        return isConstant0(formula) || isConstant1(formula);
    }

    public static boolean isConstant0(BooleanFormula formula) {
        return Zero.getInstance().equals(formula);
    }

    public static boolean isConstant1(BooleanFormula formula) {
        return One.getInstance().equals(formula);
    }

    public static VisualContact getPinByTypeAndName(VisualCircuitComponent component, IOType type, String name) {
        if (name != null) {
            for (VisualContact contact : component.getVisualContacts()) {
                if ((contact.getReferencedComponent().getIOType() == type) && name.equals(contact.getName())) {
                    return contact;
                }
            }
        }
        return null;
    }

    public static VisualContact createPort(VisualCircuit circuit, String name,
            Contact.IOType type, VisualContact.Direction direction, double x, double y) {

        VisualContact result = circuit.getOrCreatePort(name, type);
        result.setDirection(direction);
        circuit.setMathName(result, name);
        result.setRootSpacePosition(new Point2D.Double(x, y));
        return result;
    }

    public static VisualContact createPin(VisualCircuit circuit, VisualCircuitComponent component, String name,
            Contact.IOType type, VisualContact.Direction direction, double x, double y) {

        VisualContact result = component.createContact(type);
        result.setDirection(direction);
        circuit.setMathName(result, name);
        result.setRootSpacePosition(new Point2D.Double(x, y));
        return result;
    }

    public static VisualConnection connectIfPossible(VisualCircuit circuit,
            VisualContact fromContact, VisualContact toContact) {

        if ((fromContact != null) && (toContact != null)) {
            VisualConnection connection = circuit.getConnection(fromContact, toContact);
            if (connection == null) {
                try {
                    return circuit.connect(fromContact, toContact);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarning(e.getMessage());
                }
            }
        }
        return null;
    }

    public static boolean connectToHangingInputPins(VisualCircuit circuit, VisualContact srcContact,
            VisualCircuitComponent component, boolean useDriverReplica) {

        boolean result = false;
        for (VisualContact contact : component.getVisualInputs()) {
            if (circuit.getPreset(contact).isEmpty()) {
                result |= (connectIfPossible(circuit, srcContact, contact) != null);
                if (useDriverReplica) {
                    ConversionUtils.replicateDriverContact(circuit, contact);
                }
            }
        }
        return result;
    }

}
