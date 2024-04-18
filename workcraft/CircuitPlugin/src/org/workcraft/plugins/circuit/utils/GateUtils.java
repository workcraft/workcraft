package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.MixUtils;
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
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GateInterface;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
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
            VisualContact predContact) {

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
        Point2D predPoint = predContact.getRootSpacePosition();
        Point2D succPoint = MixUtils.middleRootspacePosition(succComponents);
        Point2D pos = MixUtils.middlePoint(Arrays.asList(predPoint, succPoint));
        if (pos != null) {
            component.setRootSpacePosition(pos);
        }

        VisualContact inputContact = component.getFirstVisualInput();
        VisualContact outputContact = component.getFirstVisualOutput();
        VisualContact.Direction direction = getDirection(predPoint, succPoint);
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
            throw new RuntimeException(e.getMessage());
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

    public static VisualFunctionComponent createAOI222Gate(VisualCircuit circuit) {
        return createGate(circuit,
                new GateInterface(Arrays.asList("A1", "A2", "B1", "B2", "C1", "C2"), "ON"),
                vars -> new Not(new Or(new Or(new And(vars.get(0), vars.get(1)), new And(vars.get(2), vars.get(3))),
                        new And(vars.get(4), vars.get(5)))));
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
        Pair<Gate, Map<BooleanVariable, String>> mapping = GenlibUtils.findMapping(desiredGateFunction,
                LibraryManager.getLibrary());

        String functionString;
        List<String> inputNames;
        if (mapping == null) {
            functionString = StringGenerator.toString(desiredGateFunction);
            inputNames = new ArrayList<>(defaultGateInterface.getInputs());
        } else {
            Gate gate = mapping.getFirst();
            component.setLabel(gate.name);
            functionString = gate.function.formula;
            outputName = gate.function.name;
            inputNames = inputVars.stream()
                    .map(mapping.getSecond()::get)
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
            if (!(contact instanceof FunctionContact)) continue;
            FunctionContact inputPort = (FunctionContact) contact;
            result.add(inputPort);
            result.addAll(getUsedVariables(inputPort));
        }
        return result;
    }

    public static Set<BooleanVariable> getUsedVariables(FunctionComponent component) {
        Set<BooleanVariable> result = new HashSet<>();
        for (FunctionContact contact : component.getFunctionOutputs()) {
            result.add(contact);
            result.addAll(GateUtils.getUsedVariables(contact));
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
        if (parent instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) parent;
            if (component.isBuffer()) {
                result = component;
            }
        }

        // Insert fork buffer if reuse did not work out
        if (result == null) {
            SpaceUtils.makeSpaceAfterContact(circuit, contact, 3.0);
            result = GateUtils.createBufferGate(circuit);
            GateUtils.insertGateAfter(circuit, result, contact);
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

}
