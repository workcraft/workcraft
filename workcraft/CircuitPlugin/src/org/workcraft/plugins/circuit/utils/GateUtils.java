package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.MixUtils;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.formula.workers.DumbBooleanWorker;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.BinaryGateInterface;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.types.Pair;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.BiFunction;

public class GateUtils {

    private static final BooleanWorker DUMB_WORKER = DumbBooleanWorker.getInstance();
    private static final BooleanWorker CLEVER_WORKER = CleverBooleanWorker.getInstance();

    public static void insertGateAfter(VisualCircuit circuit, VisualCircuitComponent component, VisualContact predContact) {
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
        for (VisualComponent succComponent: succComponents) {
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
    }

    public static void insertGateWithin(VisualCircuit circuit, VisualCircuitComponent component,
            VisualCircuitConnection connection) {

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
        LinkedList<Point2D> suffixControlPoints = ConnectionHelper.getSuffixControlPoints(connection, pos);
        circuit.remove(connection);
        try {
            VisualConnection inputConnection = circuit.connect(fromNode, inputContact);
            ConnectionHelper.addControlPoints(inputConnection, prefixControlPoints);
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

    public static VisualFunctionComponent createBufferGate(VisualCircuit circuit) {
        String gateName = "";
        String inName = "I";
        String outName = "O";

        FreeVariable inVar = new FreeVariable(inName);
        Pair<Gate, Map<BooleanVariable, String>> mapping = GenlibUtils.findMapping(inVar, LibraryManager.getLibrary());
        if (mapping != null) {
            Gate gate = mapping.getFirst();
            gateName = gate.name;
            Map<BooleanVariable, String> assignments = mapping.getSecond();
            inName = assignments.get(inVar);
            outName = gate.function.name;
        }

        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);
        component.setLabel(gateName);

        VisualFunctionContact inputContact = circuit.getOrCreateContact(component, inName, Contact.IOType.INPUT);
        inputContact.setPosition(new Point2D.Double(-1.5, 0.0));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, outName, Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));
        outputContact.setSetFunction(inputContact.getReferencedComponent());

        return component;
    }

    public static VisualFunctionComponent createAndGate(VisualCircuit circuit) {
        return createBinaryGate(circuit, new BinaryGateInterface("", "A", "B", "O"), And::new);
    }

    public static VisualFunctionComponent createOrGate(VisualCircuit circuit) {
        return createBinaryGate(circuit, new BinaryGateInterface("", "A", "B", "O"), Or::new);
    }

    public static VisualFunctionComponent createNandbGate(VisualCircuit circuit) {
        return createBinaryGate(circuit, new BinaryGateInterface("",  "AN", "B", "ON"),
                (var1, var2) -> new Not(new And(new Not(var1), var2)));
    }

    public static VisualFunctionComponent createNorbGate(VisualCircuit circuit) {
        return createBinaryGate(circuit, new BinaryGateInterface("",  "AN", "B", "ON"),
                (var1, var2) -> new Not(new Or(new Not(var1), var2)));
    }

    public static VisualFunctionComponent createBinaryGate(VisualCircuit circuit, BinaryGateInterface defaultGateInterface,
            BiFunction<FreeVariable, FreeVariable, BooleanFormula> func) {

        String gateName = defaultGateInterface.name;
        String outName = defaultGateInterface.output;
        String in1Name = defaultGateInterface.firstInput;
        String in2Name = defaultGateInterface.secondInput;

        FreeVariable in1Var = new FreeVariable(in1Name);
        FreeVariable in2Var = new FreeVariable(in2Name);
        BooleanFormula formula = func.apply(in1Var, in2Var);
        Pair<Gate, Map<BooleanVariable, String>> mapping = GenlibUtils.findMapping(formula, LibraryManager.getLibrary());
        if (mapping != null) {
            Gate gate = mapping.getFirst();
            gateName = gate.name;
            Map<BooleanVariable, String> assignments = mapping.getSecond();
            in1Name = assignments.get(in1Var);
            in2Name = assignments.get(in2Var);
            outName = gate.function.name;
        }

        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);
        component.setLabel(gateName);

        VisualFunctionContact firstInputContact = circuit.getOrCreateContact(component, in1Name, Contact.IOType.INPUT);
        firstInputContact.setPosition(new Point2D.Double(-1.5, -0.5));

        VisualFunctionContact secondInputContact = circuit.getOrCreateContact(component, in2Name, Contact.IOType.INPUT);
        secondInputContact.setPosition(new Point2D.Double(-1.5, 0.5));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, outName, Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        BooleanFormula setFunction = FormulaUtils.replace(formula, Arrays.asList(in1Var, in2Var),
                Arrays.asList(firstInputContact.getReferencedComponent(), (Contact) secondInputContact.getReferencedComponent()));

        outputContact.setSetFunction(setFunction);

        return component;
    }

    public static void propagateInitialState(VisualCircuit circuit, VisualFunctionComponent component) {
        propagateInitialState(circuit.getMathModel(), component.getReferencedComponent());
    }

    public static void propagateInitialState(Circuit circuit, FunctionComponent component) {
        Pair<List<BooleanVariable>, List<BooleanFormula>> variableAsignment = getVariableAssignment(circuit, component);
        for (FunctionContact output : component.getFunctionOutputs()) {
            BooleanFormula setFunction = FormulaUtils.replace(output.getSetFunction(),
                    variableAsignment.getFirst(), variableAsignment.getSecond(), CLEVER_WORKER);

            boolean isOne = One.getInstance().equals(setFunction);
            output.setInitToOne(isOne);
        }
    }

    public static boolean isExcitedComponent(Circuit circuit, FunctionComponent component) {
        Pair<List<BooleanVariable>, List<BooleanFormula>> variableAsignment = getVariableAssignment(circuit, component);
        for (FunctionContact output : component.getFunctionOutputs()) {
            BooleanFormula setFunction = FormulaUtils.replace(output.getSetFunction(),
                    variableAsignment.getFirst(), variableAsignment.getSecond(), CLEVER_WORKER);

            if ((setFunction != null) && (One.getInstance().equals(setFunction) != output.getInitToOne())) {
                BooleanFormula resetFunction = FormulaUtils.replace(output.getResetFunction(),
                        variableAsignment.getFirst(), variableAsignment.getSecond(), CLEVER_WORKER);

                if ((resetFunction == null) || (One.getInstance().equals(resetFunction) == output.getInitToOne())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Pair<List<BooleanVariable>, List<BooleanFormula>> getVariableAssignment(
            Circuit circuit, FunctionComponent component) {

        List<BooleanVariable> variables = new LinkedList<>();
        List<BooleanFormula> values = new LinkedList<>();
        for (FunctionContact input : component.getFunctionInputs()) {
            Pair<Contact, Boolean> pair = CircuitUtils.findDriverAndInversionSkipZeroDelay(circuit, input);
            if (pair != null) {
                boolean initToOne = pair.getFirst().getInitToOne();
                boolean inversion = pair.getSecond();
                variables.add(input);
                BooleanFormula state = initToOne ^ inversion ? One.getInstance() : Zero.getInstance();
                values.add(state);
            }
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
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, "o", Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        List<Contact> vars = createGateInputs(circuit, component, count);
        BooleanFormula function = FormulaUtils.createAnd(vars, DUMB_WORKER);
        outputContact.setSetFunction(function);
        return component;
    }

    public static VisualFunctionComponent createOrGate(VisualCircuit circuit, int count) {
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, "o", Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        List<Contact> vars = createGateInputs(circuit, component, count);
        BooleanFormula function = FormulaUtils.createOr(vars, DUMB_WORKER);
        outputContact.setSetFunction(function);
        return component;
    }

    private static List<Contact> createGateInputs(VisualCircuit circuit, VisualFunctionComponent component, int count) {
        List<Contact> vars = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            VisualFunctionContact inputContact = circuit.getOrCreateContact(component, "i" + i, Contact.IOType.INPUT);
            inputContact.setPosition(new Point2D.Double(-1.5, i - 0.5 * (count - 1)));
            vars.add(inputContact.getReferencedComponent());
        }
        return vars;
    }

}
