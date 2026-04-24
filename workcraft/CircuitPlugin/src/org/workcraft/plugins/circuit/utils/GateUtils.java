package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.formula.workers.DumbBooleanWorker;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.SortUtils;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GateUtils {

    private static final BooleanWorker DUMB_WORKER = DumbBooleanWorker.getInstance();
    private static final BooleanWorker CLEVER_WORKER = CleverBooleanWorker.getInstance();

    private GateUtils() {
    }

    public static void insertGateAfter(VisualCircuit circuit, VisualCircuitComponent component,
            VisualContact predContact, double offset) {

        Container container = (Container) predContact.getParent();
        // Step up in the hierarchy for a self-loop
        if (container instanceof VisualCircuitComponent) {
            container = (Container) container.getParent();
        }
        circuit.reparent(container, circuit, circuit.getRoot(), Collections.singletonList(component));

        LinkedList<VisualComponent> succComponents = new LinkedList<>();
        for (VisualNode succNode : circuit.getPostset(predContact)) {
            if (succNode instanceof VisualComponent) {
                succComponents.add((VisualComponent) succNode);
            }
        }

        VisualContact.Direction direction = predContact.isPin()
                ? predContact.getDirection() : predContact.getDirection().flip();

        Point2D.Double pos = new Point2D.Double(
                predContact.getRootSpaceX() + direction.getGradientX() * offset,
                predContact.getRootSpaceY() + direction.getGradientY() * offset);

        component.setRootSpacePosition(pos);
        VisualContact inputContact = component.getFirstVisualInput();
        VisualContact outputContact = component.getFirstVisualOutput();
        outputContact.setDirection(direction);

        try {
            circuit.connect(predContact, inputContact);
        } catch (InvalidConnectionException e) {
            LogUtils.logWarning(e.getMessage());
        }
        for (VisualComponent succComponent : succComponents) {
            VisualConnection connection = circuit.getConnection(predContact, succComponent);
            LinkedList<Point2D> suffixControlPoints = ConnectionHelper.getSuffixControlPoints(connection, pos);
            circuit.remove(connection);
            try {
                VisualConnection outputConnection = circuit.connect(outputContact, succComponent);
                outputConnection.copyStyle(connection);
                ConnectionHelper.addControlPoints(outputConnection, suffixControlPoints);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarning(e.getMessage());
            }
        }
        ConversionUtils.updateReplicas(circuit, predContact, outputContact);
    }

    public static void insertGateBefore(VisualCircuit circuit, VisualCircuitComponent component,
            VisualContact succContact, double offset) {

        Container container = (Container) succContact.getParent();
        // Step up in the hierarchy for a self-loop
        if (container instanceof VisualCircuitComponent) {
            container = (Container) container.getParent();
        }
        circuit.reparent(container, circuit, circuit.getRoot(), Collections.singletonList(component));

        Point2D.Double pos = new Point2D.Double(
                succContact.getRootSpaceX() + succContact.getDirection().getGradientX() * offset,
                succContact.getRootSpaceY() + succContact.getDirection().getGradientY() * offset);

        component.setRootSpacePosition(pos);
        VisualContact inputContact = component.getFirstVisualInput();
        VisualContact outputContact = component.getFirstVisualOutput();
        outputContact.setDirection(succContact.getDirection().flip());

        for (VisualNode predNode : circuit.getPreset(succContact)) {
            VisualConnection connection = circuit.getConnection(predNode, succContact);
            LinkedList<Point2D> prefixControlPoints = ConnectionHelper.getPrefixControlPoints(connection, pos);
            try {
                VisualConnection inputConnection = circuit.connect(predNode, inputContact);
                inputConnection.copyStyle(connection);
                ConnectionHelper.addControlPoints(inputConnection, prefixControlPoints);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarning(e.getMessage());
            }
            circuit.remove(connection);
        }
        try {
            circuit.connect(outputContact, succContact);
        } catch (InvalidConnectionException e) {
            LogUtils.logWarning(e.getMessage());
        }
    }

    public static void insertGateWithin(VisualCircuit circuit, VisualCircuitComponent component,
            VisualConnection connection) {

        VisualNode fromNode = connection.getFirst();
        VisualNode toNode = connection.getSecond();
        Container container = Hierarchy.getNearestContainer(fromNode, toNode);
        // Step up in the hierarchy for a self-loop
        if (container instanceof VisualCircuitComponent) {
            container = (Container) container.getParent();
        }
        circuit.reparent(container, circuit, circuit.getRoot(), Collections.singletonList(component));

        Point2D pos = connection.getMiddleSegmentCenterPoint();
        component.setPosition(pos);

        Point2D predPoint = ConnectionHelper.getPredPoint(connection, pos);
        Point2D succPoint = ConnectionHelper.getSuccPoint(connection, pos);
        VisualContact.Direction direction = getDirection(predPoint, succPoint);

        VisualContact inputContact = component.getFirstVisualInput();
        VisualContact outputContact = component.getFirstVisualOutput();
        outputContact.setDirection(direction);

        LinkedList<Point2D> prefixControlPoints = ConnectionHelper.getPrefixControlPoints(connection, pos);
        try {
            VisualConnection inputConnection = circuit.connect(fromNode, inputContact);
            ConnectionHelper.addControlPoints(inputConnection, prefixControlPoints);
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e.getMessage());
        }
        LinkedList<Point2D> suffixControlPoints = ConnectionHelper.getSuffixControlPoints(connection, pos);
        // Original connection must be removed at this point:
        // * AFTER creating a new connection from its first node (so first node is not automatically cleared out)
        // * BEFORE creating a connection to the second node (as only one driver is allowed)
        circuit.remove(connection);
        try {
            VisualConnection outputConnection = circuit.connect(outputContact, toNode);
            ConnectionHelper.addControlPoints(outputConnection, suffixControlPoints);
        } catch (InvalidConnectionException e) {
            LogUtils.logWarning(e.getMessage());
        }
    }

    private static VisualContact.Direction getDirection(Point2D predPoint, Point2D succPoint) {
        if ((predPoint == null) || (succPoint == null)) {
            return VisualContact.Direction.EAST;
        } else {
            double dx = succPoint.getX() - predPoint.getX();
            double dy = succPoint.getY() - predPoint.getY();
            if (Math.abs(dx) > Math.abs(dy)) {
                return dx > 0 ? VisualContact.Direction.EAST : VisualContact.Direction.WEST;
            } else {
                return dy > 0 ? VisualContact.Direction.SOUTH : VisualContact.Direction.NORTH;
            }
        }
    }

    public static VisualFunctionComponent createConst1Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Collections.emptyList(), "O"),
                vars -> One.getInstance());
    }

    public static VisualFunctionComponent createConst0Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Collections.emptyList(), "O"),
                vars -> Zero.getInstance());
    }

    public static VisualFunctionComponent createBufferGate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Collections.singletonList("I"), "O"),
                vars -> vars.get(0));
    }

    public static VisualFunctionComponent createInverterGate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Collections.singletonList("I"), "ON"),
                vars -> new Not(vars.get(0)));
    }

    public static VisualFunctionComponent createAnd2Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("A", "B"), "O"),
                vars -> FormulaUtils.createAnd(vars, DUMB_WORKER));
    }

    public static VisualFunctionComponent createAnd3Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("A", "B", "C"), "O"),
                vars -> FormulaUtils.createAnd(vars, DUMB_WORKER));
    }

    public static VisualFunctionComponent createOr2Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("A", "B"), "O"),
                vars -> FormulaUtils.createOr(vars, DUMB_WORKER));
    }

    public static VisualFunctionComponent createOr3Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("A", "B", "C"), "O"),
                vars -> FormulaUtils.createOr(vars, DUMB_WORKER));
    }

    public static VisualFunctionComponent createNand2Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("A", "B"), "ON"),
                vars -> new Not(FormulaUtils.createAnd(vars, DUMB_WORKER)));
    }

    public static VisualFunctionComponent createNand3Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("A", "B", "C"), "ON"),
                vars -> new Not(FormulaUtils.createAnd(vars, DUMB_WORKER)));
    }

    public static VisualFunctionComponent createNor2Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("A", "B"), "ON"),
                vars -> new Not(FormulaUtils.createOr(vars, DUMB_WORKER)));
    }

    public static VisualFunctionComponent createNor3Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("A", "B", "C"), "ON"),
                vars -> new Not(FormulaUtils.createOr(vars, DUMB_WORKER)));
    }

    public static VisualFunctionComponent createNand2bGate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("AN", "B"), "ON"),
                vars -> new Not(new And(new Not(vars.get(0)), vars.get(1))));
    }

    public static VisualFunctionComponent createNor2bGate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("AN", "B"), "ON"),
                vars -> new Not(new Or(new Not(vars.get(0)), vars.get(1))));
    }

    public static VisualFunctionComponent createGate(VisualCircuit circuit, GateInterface defaultGateInterface,
            Function<List<FreeVariable>, BooleanFormula> func) {

        VisualFunctionComponent component = circuit.createVisualComponent(
                new FunctionComponent(), VisualFunctionComponent.class);

        String outputName = defaultGateInterface.getOutput();
        List<FreeVariable> inputVars = defaultGateInterface.getInputs().stream()
                .map(FreeVariable::new)
                .collect(Collectors.toList());

        BooleanFormula desiredGateFunction = func.apply(inputVars);
        Gate.Mapping mapping = GenlibUtils.findMapping(desiredGateFunction,
                LibraryManager.getLibrary());

        String functionString;
        List<String> inputNames;
        if (mapping == null) {
            functionString = StringGenerator.toString(desiredGateFunction);
            inputNames = new ArrayList<>(defaultGateInterface.getInputs());
        } else {
            Gate gate = mapping.gate();
            component.setLabel(gate.name);
            functionString = gate.function.formula;
            outputName = gate.function.name;
            Gate.PinRenamining pinRenamining = mapping.pinRenamining();
            inputNames = inputVars.stream()
                    .map(pinRenamining::get)
                    .collect(Collectors.toList());
        }

        double y = -0.5 * (inputNames.size() - 1);
        for (String inputName : inputNames) {
            VisualContact inputContact = circuit.getOrCreateContact(component, inputName, Contact.IOType.INPUT);
            inputContact.setPosition(new Point2D.Double(-1.5, y));
            y += 1.0;
        }
        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, outputName, Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));
        try {
            outputContact.setSetFunction(CircuitUtils.parsePinFunction(circuit, component, functionString));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return component;
    }

    public static void propagateInitialState(VisualCircuit circuit, VisualFunctionComponent component) {
        Set<VisualFunctionContact> outputContacts = component.getVisualFunctionContacts().stream()
                .filter(VisualContact::isOutput).collect(Collectors.toSet());

        propagateInitialState(circuit, component, outputContacts);
    }

    public static void propagateInitialState(VisualCircuit circuit, VisualFunctionComponent component,
            Collection<VisualFunctionContact> contacts) {

        Collection<FunctionContact> mathContacts = contacts.stream()
                .map(VisualFunctionContact::getReferencedComponent).collect(Collectors.toSet());

        propagateInitialState(circuit.getMathModel(), component.getReferencedComponent(), mathContacts);
    }

    public static void propagateInitialState(Circuit circuit, FunctionComponent component,
            Collection<FunctionContact> contacts) {

        if (!contacts.isEmpty()) {
            Pair<List<BooleanVariable>, List<BooleanFormula>> varAssignment = getVariableAssignment(circuit, component);
            for (FunctionContact contact : contacts) {
                BooleanFormula setFunction = FormulaUtils.replace(contact.getSetFunction(),
                        varAssignment.getFirst(), varAssignment.getSecond(), CLEVER_WORKER);

                boolean isOne = One.getInstance().equals(setFunction);
                contact.setInitToOne(isOne);
            }
        }
    }

    public static boolean isExcitedComponent(Circuit circuit, FunctionComponent component) {
        Pair<List<BooleanVariable>, List<BooleanFormula>> varAssignment = getVariableAssignment(circuit, component);
        for (FunctionContact output : component.getFunctionOutputs()) {
            BooleanFormula setFunction = FormulaUtils.replace(output.getSetFunction(),
                    varAssignment.getFirst(), varAssignment.getSecond(), CLEVER_WORKER);

            if ((setFunction != null) && (One.getInstance().equals(setFunction) != output.getInitToOne())) {
                BooleanFormula resetFunction = FormulaUtils.replace(output.getResetFunction(),
                        varAssignment.getFirst(), varAssignment.getSecond(), CLEVER_WORKER);

                if ((resetFunction == null) || (One.getInstance().equals(resetFunction) == output.getInitToOne())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Pair<List<BooleanVariable>, List<BooleanFormula>> getVariableAssignment(
            Circuit circuit, FunctionComponent component) {

        List<BooleanVariable> variables = new LinkedList<>();
        List<BooleanFormula> values = new LinkedList<>();
        for (FunctionContact input : component.getFunctionInputs()) {
            variables.add(input);
            boolean state = CircuitUtils.findInitToOneFromDriver(circuit, input);
            values.add(state ? One.getInstance() : Zero.getInstance());
        }
        return Pair.of(variables, values);
    }

    public static Set<BooleanVariable> getUsedPortVariables(Circuit circuit) {
        Set<BooleanVariable> result = new HashSet<>();
        for (Contact contact : circuit.getInputPorts()) {
            if (!(contact instanceof FunctionContact inputPort)) continue;
            result.add(inputPort);
            result.addAll(getUsedVariables(inputPort));
        }
        return result;
    }

    public static Set<BooleanVariable> getUsedVariables(FunctionComponent component) {
        Set<BooleanVariable> result = new HashSet<>();
        for (FunctionContact contact : component.getFunctionOutputs()) {
            result.add(contact);
            result.addAll(getUsedVariables(contact));
        }
        return result;
    }

    public static Set<BooleanVariable> getUsedVariables(FunctionContact contact) {
        HashSet<BooleanVariable> literals = new HashSet<>();
        BooleanFormula setFunction = contact.getSetFunction();
        if (setFunction != null) {
            literals.addAll(FormulaUtils.extractOrderedVariables(setFunction));
        }
        BooleanFormula resetFunction = contact.getResetFunction();
        if (resetFunction != null) {
            literals.addAll(FormulaUtils.extractOrderedVariables(resetFunction));
        }
        return literals;
    }

    public static VisualFunctionComponent createAndGate(VisualCircuit circuit, int count) {
        VisualFunctionComponent component = circuit.createVisualComponent(
                new FunctionComponent(), VisualFunctionComponent.class);

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, "o", Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        List<Contact> vars = createGateInputs(circuit, component, count);
        BooleanFormula function = FormulaUtils.createAnd(vars, DUMB_WORKER);
        outputContact.setSetFunction(function);
        return component;
    }

    public static VisualFunctionComponent createOrGate(VisualCircuit circuit, int count) {
        VisualFunctionComponent component = circuit.createVisualComponent(
                new FunctionComponent(), VisualFunctionComponent.class);

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, "o", Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        List<Contact> vars = createGateInputs(circuit, component, count);
        BooleanFormula function = FormulaUtils.createOr(vars, DUMB_WORKER);
        outputContact.setSetFunction(function);
        return component;
    }

    private static List<Contact> createGateInputs(VisualCircuit circuit, VisualFunctionComponent component, int count) {
        List<Contact> vars = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String name = "i" + index;
            VisualFunctionContact inputContact = circuit.getOrCreateContact(component, name, Contact.IOType.INPUT);
            inputContact.setPosition(new Point2D.Double(-1.5, index - 0.5 * (count - 1)));
            vars.add(inputContact.getReferencedComponent());
        }
        return vars;
    }

    public static VisualFunctionComponent insertOrReuseBuffer(VisualCircuit circuit, VisualContact contact) {
        VisualFunctionComponent result = null;
        // Try to reuse existing buffer or inverter
        Node parent = contact.getParent();
        if (parent instanceof VisualFunctionComponent component) {
            if (component.isBuffer()) {
                result = component;
            }
        }

        // Insert fork buffer if reuse did not work out
        if (result == null) {
            SpaceUtils.makeSpaceAroundContact(circuit, contact, 3.0);
            result = createBufferGate(circuit);
            insertGateAfter(circuit, result, contact, 2.0);
            VisualFunctionContact gateOutput = result.getGateOutput();
            gateOutput.setInitToOne(contact.getInitToOne());
        }
        return result;
    }

    public static VisualFunctionComponent createComplexGateComponent(VisualCircuit circuit,
            String outputName, BooleanFormula function) {

        VisualFunctionComponent component = circuit.createVisualComponent(
                new FunctionComponent(), VisualFunctionComponent.class);

        List<BooleanVariable> functionVars = SortUtils.getSortedNatural(
                FormulaUtils.extractVariables(function), BooleanVariable::getLabel);

        List<BooleanVariable> gateVars = new ArrayList<>();
        double y = -0.5 * (functionVars.size() - 1);
        for (BooleanVariable variable : functionVars) {
            VisualContact inputPin = circuit.getOrCreateContact(component, variable.getLabel(), Contact.IOType.INPUT);
            inputPin.setPosition(new Point2D.Double(-2.0, y));
            gateVars.add(inputPin.getReferencedComponent());
            y += 1.0;
        }
        BooleanFormula gateFunction = FormulaUtils.replace(function, functionVars, gateVars);

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, outputName, Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(2.0, 0.0));
        outputContact.setSetFunction(gateFunction);
        return component;
    }

    public static void renameGatePins(VisualCircuit circuit, VisualFunctionComponent component, Gate.Mapping mapping) {
        Map<BooleanVariable, String> pinRenamining = mapping.pinRenamining();
        for (VisualContact inpitPin : component.getVisualInputs()) {
            String pinName = pinRenamining.get(inpitPin.getReferencedComponent());
            if (pinName != null) {
                circuit.setMathName(inpitPin, pinName);
            }
        }
        VisualFunctionContact outputPin = component.getGateOutput();
        if (outputPin != null) {
            circuit.setMathName(outputPin, mapping.gate().function.name);
        }
    }

    public static VisualContact convertGate(VisualCircuit circuit, VisualFunctionComponent component,
            Gate.ExtendedMapping extendedMapping) {

        insertExtraInputPin(circuit, component, extendedMapping);
        Map<BooleanVariable, BooleanFormula> inputVarReplacements = convertInputPins(circuit, component, extendedMapping);
        VisualFunctionContact outputPin = component.getGateOutput();
        VisualContact result = convertOutputPin(circuit, outputPin, extendedMapping);
        updateFunction(circuit, outputPin, inputVarReplacements, extendedMapping);
        component.getReferencedComponent().setModule(extendedMapping.gate().name);
        return result;
    }

    private static void insertExtraInputPin(VisualCircuit circuit, VisualFunctionComponent component,
            Gate.ExtendedMapping extendedMapping) {

        Map<BooleanVariable, String> pinRenames = extendedMapping.pinRenamining();
        Set<String> invertedPinNames = extendedMapping.invertedPinNames();
        Map<String, BooleanVariable> extraPinAssignment = extendedMapping.extraPinAssignment();

        for (String extraPinName : extraPinAssignment.keySet()) {
            VisualFunctionContact extraInputPin = component.createContact(Contact.IOType.INPUT);
            pinRenames.put(extraInputPin.getReferencedComponent(), extraPinName);
            if (extraPinAssignment.get(extraPinName) instanceof Contact originalVar) {
                VisualFunctionContact originalInputPin = circuit.getVisualComponent(originalVar, VisualFunctionContact.class);
                Contact directDriverVar = CircuitUtils.findDriver(circuit.getMathModel(), originalVar, false);
                if (directDriverVar == null) {
                    connectFromCommonJoint(circuit, originalInputPin, extraInputPin);
                } else if (!directDriverVar.isZeroDelayDriver()) {
                    VisualConnection originalConnection = circuit.getConnections(originalInputPin).iterator().next();
                    connectFromConnectionIfPossible(circuit, originalConnection, extraInputPin);
                } else {
                    Pair<Contact, Boolean> throughZeroDelayDriverAndInversion =
                            CircuitUtils.findDriverAndInversionSkipZeroDelay(circuit.getMathModel(), originalVar);

                    if (throughZeroDelayDriverAndInversion == null) continue;

                    connectFromVarIfPossible(circuit, throughZeroDelayDriverAndInversion.getFirst(), extraInputPin);
                    if (throughZeroDelayDriverAndInversion.getSecond()) {
                        invertedPinNames.remove(extraPinName);
                    }
                }
            }
        }
    }

    private static void connectFromCommonJoint(VisualCircuit circuit,
            VisualFunctionContact firstPin, VisualFunctionContact secondPin) {

        if ((firstPin != null) && (secondPin != null)) {
            Container container = Hierarchy.getNearestContainer(firstPin, secondPin);
            VisualJoint joint = circuit.createJoint(container);
            joint.setRootSpacePosition(firstPin.getRootSpacePosition());
            try {
                circuit.connect(joint, firstPin);
                circuit.connect(joint, secondPin);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarning(e.getMessage());
            }
        }
    }

    private static void connectFromVarIfPossible(VisualCircuit circuit, Contact fromVar, VisualContact toContact) {
        VisualContact fromContact = circuit.getVisualComponent(fromVar, VisualContact.class);
        if ((fromContact != null) && (toContact != null)) {
            try {
                circuit.connect(fromContact, toContact);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarning(e.getMessage());
            }
        }
    }

    private static void connectFromConnectionIfPossible(VisualCircuit circuit,
            VisualConnection fromConnection, VisualFunctionContact toContact) {

        if ((fromConnection != null) && (toContact != null)) {
            if (fromConnection.getSecond() instanceof VisualContact contact) {
                fromConnection.setSplitPoint(contact.getRootSpacePosition());
            }
            try {
                circuit.connect(fromConnection, toContact);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarning(e.getMessage());
            }
        }
    }

    private static Map<BooleanVariable, BooleanFormula> convertInputPins(VisualCircuit circuit,
            VisualFunctionComponent component, Gate.ExtendedMapping extendedMapping) {

        Map<BooleanVariable, BooleanFormula> result = new HashMap<>();
        Map<BooleanVariable, String> pinRenames = extendedMapping.pinRenamining();
        Set<String> invertedPinNames = extendedMapping.invertedPinNames();

        for (VisualContact inputPin : component.getVisualInputs()) {
            Contact inputPinVar = inputPin.getReferencedComponent();
            String newInputPinName = pinRenames.get(inputPinVar);
            if (newInputPinName == null) {
                continue;
            }
            circuit.setMathName(inputPin, Identifier.addInternalPrefix(newInputPinName));
            if (invertedPinNames.contains(newInputPinName)) {
                result.put(inputPinVar, new Not(inputPinVar));
            }
        }

        boolean detachInvertersAsZeroDelay = !component.getIsEnvironment();
        for (VisualContact inputPin : component.getVisualInputs()) {
            Contact inputPinVar = inputPin.getReferencedComponent();
            String newInputPinName = pinRenames.get(inputPinVar);
            if (newInputPinName == null) {
                continue;
            }
            circuit.setMathName(inputPin, newInputPinName);
            if (invertedPinNames.contains(newInputPinName)) {
                VisualFunctionComponent trivialDriverComponent = getDedicatedTrivialDriverOrNull(circuit, inputPin);
                if (trivialDriverComponent == null) {
                    VisualFunctionComponent newInverter = createInverterGate(circuit);
                    newInverter.copyStylePreserveMapping(component);
                    SpaceUtils.makeSpaceAroundContact(circuit, inputPin, 2.0);
                    insertGateBefore(circuit, newInverter, inputPin, 1.5);
                    newInverter.setIsZeroDelay(detachInvertersAsZeroDelay);
                } else if (trivialDriverComponent.isInverter()) {
                    ContractionUtils.contractComponentIfPossible(circuit, trivialDriverComponent);
                } else if (trivialDriverComponent.isBuffer()) {
                    convertBufferToInverter(circuit, trivialDriverComponent, false);
                    trivialDriverComponent.setIsZeroDelay(true);
                } else if (trivialDriverComponent.isTie1()) {
                    convertTie1ToTie0(circuit, trivialDriverComponent);
                } else if (trivialDriverComponent.isTie0()) {
                    convertTie0ToTie1(circuit, trivialDriverComponent);
                }
            }
        }
        return result;
    }

    private static VisualFunctionComponent getDedicatedTrivialDriverOrNull(
            VisualCircuit circuit, VisualContact contact) {

        VisualContact driverContact = CircuitUtils.findDriver(circuit, contact, false);
        if (driverContact != null) {
            Set<VisualContact> drivenContacts = CircuitUtils.findDriven(circuit, driverContact);
            if (drivenContacts.size() == 1) {
                Node parent = driverContact.getParent();
                if ((parent instanceof VisualFunctionComponent drivenComponent)
                        && (drivenComponent.isInverter() || drivenComponent.isBuffer()
                        || drivenComponent.isTie1() || drivenComponent.isTie0())) {

                    return drivenComponent;
                }
            }
        }
        return null;
    }

    private static VisualContact convertOutputPin(VisualCircuit circuit, VisualFunctionContact outputPin,
            Gate.ExtendedMapping extendedMapping) {

        VisualContact result = outputPin;
        String newOutputPinName = extendedMapping.gate().function.name;
        circuit.setMathName(outputPin, newOutputPinName);

        Set<String> invertedPinNames = extendedMapping.invertedPinNames();
        if (invertedPinNames.contains(newOutputPinName)) {
            VisualFunctionComponent trivialDrivenComponent
                    = getDedicatedTrivialDrivenComponentWithCorrectInitOrNull(circuit, outputPin);

            if (trivialDrivenComponent == null) {
                // Detach output inverter from the gate
                VisualFunctionComponent newInverter = createInverterGate(circuit);
                SpaceUtils.makeSpaceAroundContact(circuit, outputPin, 3.0);
                insertGateAfter(circuit, newInverter, outputPin, 2.0);
                // Inherit initial state of the inverter from the original gate
                result = newInverter.getGateOutput();
                result.setInitToOne(outputPin.getInitToOne());
                // Propagate initial state of the gate from its inputs (its forced initial state is now held by the inverter)
                if (outputPin.getParent() instanceof VisualFunctionComponent component) {
                    newInverter.copyStylePreserveMapping(component);
                    GateUtils.propagateInitialState(circuit, component);
                }
                // Move path-breaker and force-init tags from the original gate to the detached inverter
                result.setPathBreaker(outputPin.getPathBreaker());
                outputPin.setPathBreaker(false);
                result.setForcedInit(outputPin.getForcedInit());
                outputPin.setForcedInit(false);
            } else if (trivialDrivenComponent.isInverter()) {
                ContractionUtils.contractComponentIfPossible(circuit, trivialDrivenComponent);
            } else if (trivialDrivenComponent.isBuffer()) {
                convertBufferToInverter(circuit, trivialDrivenComponent, true);
            }
            outputPin.setInitToOne(!outputPin.getInitToOne());
        }
        return result;
    }

    private static VisualFunctionComponent getDedicatedTrivialDrivenComponentWithCorrectInitOrNull(
            VisualCircuit circuit, VisualFunctionContact contact) {

        Set<VisualContact> drivenContacts = CircuitUtils.findDriven(circuit, contact);
        if (drivenContacts.size() == 1) {
            VisualContact drivenContact = drivenContacts.iterator().next();
            Node parent = drivenContact.getParent();
            if (parent instanceof VisualFunctionComponent drivenComponent) {
                VisualFunctionContact outputPin = drivenComponent.getMainVisualOutput();
                if (outputPin != null) {
                    boolean isSameInit = (outputPin.getInitToOne() == contact.getInitToOne());
                    if ((drivenComponent.isInverter() && !isSameInit) || (drivenComponent.isBuffer() && isSameInit)) {
                        return drivenComponent;
                    }
                }
            }
        }
        return null;
    }

    private static void updateFunction(VisualCircuit circuit, VisualFunctionContact outputPin,
            Map<BooleanVariable, BooleanFormula> inputVarReplacements, Gate.ExtendedMapping extendedMapping) {

        Gate gate = extendedMapping.gate();
        BooleanFormula newSetFunction = null;
        BooleanFormula newResetFunction = null;
        if (!extendedMapping.extraPinAssignment().isEmpty()) {
            try {
                newSetFunction = CircuitUtils.parseContactFunction(circuit, outputPin, gate.getSetExpression());
                newResetFunction = CircuitUtils.parseContactFunction(circuit, outputPin, gate.getResetExpression());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            newSetFunction = FormulaUtils.replace(outputPin.getSetFunction(),
                    inputVarReplacements, CleverBooleanWorker.getInstance());

            newResetFunction = FormulaUtils.replace(outputPin.getResetFunction(),
                    inputVarReplacements, CleverBooleanWorker.getInstance());

            if (extendedMapping.invertedPinNames().contains(outputPin.getName())) {
                newSetFunction = FormulaUtils.invert(newSetFunction);
                newResetFunction = FormulaUtils.invert(newResetFunction);
            }
        }

        if ((newSetFunction instanceof Not) != (gate.getSetFormula() instanceof Not)) {
            newSetFunction = FormulaUtils.propagateInversion(newSetFunction);
            newResetFunction = FormulaUtils.propagateInversion(newResetFunction);
        }
        outputPin.setBothFunctions(newSetFunction, newResetFunction);
    }

    private static void convertBufferToInverter(VisualCircuit circuit, VisualFunctionComponent component,
            boolean preserveInitToOne) {

        if (!component.isBuffer()) {
            throw new RuntimeException("Buffer is expected");
        }

        BooleanVariable inputVar = new FreeVariable("I");
        BooleanFormula formula = new Not(inputVar);
        Gate.Mapping mapping = GenlibUtils.findMapping(formula, LibraryManager.getLibrary());
        if (mapping == null) {
            throw new RuntimeException("Cannot find inverter gate mapping");
        }

        Gate gate = mapping.gate();
        component.getReferencedComponent().setModule(gate.name);
        VisualFunctionContact outputPin = component.getFirstVisualOutput();
        circuit.setMathName(outputPin, gate.function.name);

        Map<BooleanVariable, String> inputRenames = mapping.pinRenamining();
        String inputName = inputRenames.get(inputVar);
        if (inputName != null) {
            circuit.setMathName(component.getFirstVisualInput(), inputName);
        }
        outputPin.setSetFunction(FormulaUtils.invert(outputPin.getSetFunction()));
        if (!preserveInitToOne) {
            outputPin.setInitToOne(!outputPin.getInitToOne());
        }
    }

    private static void convertTie1ToTie0(VisualCircuit circuit, VisualFunctionComponent component) {
        if (!component.isTie1()) {
            throw new RuntimeException("Tie1 is expected");
        }

        BooleanFormula formula = Zero.getInstance();
        Gate.Mapping mapping = GenlibUtils.findMapping(formula, LibraryManager.getLibrary());
        if (mapping == null) {
            throw new RuntimeException("Cannot find tie0 gate mapping");
        }

        Gate gate = mapping.gate();
        component.getReferencedComponent().setModule(gate.name);
        VisualFunctionContact outputPin = component.getFirstVisualOutput();
        circuit.setMathName(outputPin, gate.function.name);

        outputPin.setSetFunction(Zero.getInstance());
        outputPin.setInitToOne(false);
    }

    private static void convertTie0ToTie1(VisualCircuit circuit, VisualFunctionComponent component) {
        if (!component.isTie0()) {
            throw new RuntimeException("Tie0 is expected");
        }

        BooleanFormula formula = One.getInstance();
        Gate.Mapping mapping = GenlibUtils.findMapping(formula, LibraryManager.getLibrary());
        if (mapping == null) {
            throw new RuntimeException("Cannot find tie0 gate mapping");
        }

        Gate gate = mapping.gate();
        component.getReferencedComponent().setModule(gate.name);
        VisualFunctionContact outputPin = component.getFirstVisualOutput();
        circuit.setMathName(outputPin, gate.function.name);

        outputPin.setSetFunction(One.getInstance());
        outputPin.setInitToOne(true);
    }

    public static VisualFunctionComponent reuseAdjacentInverterOrBuffer(VisualCircuit circuit, VisualContact contact) {
        if (contact.getParent() instanceof VisualFunctionComponent component) {
            if (component.isBuffer() || component.isInverter()) {
                return component;
            }
        }
        if (!contact.isOutput()) {
            return null;
        }
        Collection<VisualContact> drivenContacts = CircuitUtils.findDriven(circuit, contact, false);
        if (drivenContacts.size() != 1) {
            return null;
        }
        VisualContact drivenContact = drivenContacts.iterator().next();
        if (drivenContact == contact) {
            return null;
        }
        return reuseAdjacentInverterOrBuffer(circuit, drivenContact);
    }

    public static VisualFunctionComponent detachInverterOrBuffer(VisualCircuit circuit, VisualFunctionContact contact) {
        VisualFunctionComponent result = null;
        // Use negated gate (with inverter) if it is smaller than the original gate
        if ((contact.getParent() instanceof VisualFunctionComponent component)
                && component.isGate() && component.isCell()) {

            Library gateLibrary = LibraryManager.getLibrary();
            BooleanFormula setFunction = contact.getSetFunction();
            BooleanFormula resetFunction = contact.getResetFunction();
            Gate.Mapping posMapping = GenlibUtils.findMapping(setFunction, resetFunction, gateLibrary);
            BooleanFormula notSetFunction = FormulaUtils.invert(setFunction);
            BooleanFormula notResetFunction = FormulaUtils.invert(resetFunction);
            Gate.Mapping negMapping = GenlibUtils.findMapping(notSetFunction, notResetFunction, gateLibrary);
            boolean isMapped = component.isMapped();
            if ((posMapping != null) && (negMapping != null) && (posMapping.gate().size > negMapping.gate().size)) {
                contact.setBothFunctions(notSetFunction, notResetFunction);
                renameGatePins(circuit, component, negMapping);
                if (isMapped) {
                    component.getReferencedComponent().setModule(negMapping.gate().name);
                }
                contact.setInitToOne(!contact.getInitToOne());
                result = createInverterGate(circuit);
            }
        }
        // If negative gate with inverter did not work out, then use buffer
        if (result == null) {
            result = createBufferGate(circuit);
        }
        // Insert inverter/buffer after the given contact
        SpaceUtils.makeSpaceAroundContact(circuit, contact, 3.0);
        insertGateAfter(circuit, result, contact, 2.0);
        propagateInitialState(circuit, result);
        return result;
    }

}
