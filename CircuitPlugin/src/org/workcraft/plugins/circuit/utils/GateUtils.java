package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.MixUtils;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.formula.utils.LiteralsExtractor;
import org.workcraft.plugins.circuit.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;

import java.awt.geom.Point2D;
import java.util.*;

public class GateUtils {

    public static void insertGateAfter(VisualCircuit circuit, VisualCircuitComponent component, VisualContact predContact) {
        Container container = (Container) predContact.getParent();
        // Step up in the hierarchy for a self-loop
        if (container instanceof VisualCircuitComponent) {
            container = (Container) container.getParent();
        }
        circuit.reparent(container, circuit, circuit.getRoot(), Arrays.asList(component));

        LinkedList<VisualComponent> succComponents = new LinkedList<>();
        for (VisualNode succNode : circuit.getPostset(predContact)) {
            if (succNode instanceof VisualComponent) {
                succComponents.add((VisualComponent) succNode);
            }
        }
        Point2D predPoint = predContact.getRootSpacePosition();
        Point2D succPoint = MixUtils.middleRootspacePosition(succComponents);
        Point2D pos = MixUtils.middlePoint(Arrays.asList(predPoint, succPoint));
        component.setRootSpacePosition(pos);

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
        circuit.reparent(container, circuit, circuit.getRoot(), Arrays.asList(component));

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
        Gate2 gate = CircuitSettings.parseBufData();
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);
        component.setLabel(gate.name);

        VisualFunctionContact inputContact = circuit.getOrCreateContact(component, gate.in, Contact.IOType.INPUT);
        inputContact.setPosition(new Point2D.Double(-1.5, 0.0));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, gate.out, Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));
        outputContact.setSetFunction(inputContact.getReferencedContact());

        return component;
    }

    public static VisualFunctionComponent createAndGate(VisualCircuit circuit) {
        Gate3 gate = CircuitSettings.parseAndData();
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);
        component.setLabel(gate.name);

        VisualFunctionContact firstInputContact = circuit.getOrCreateContact(component, gate.in1, Contact.IOType.INPUT);
        firstInputContact.setPosition(new Point2D.Double(-1.5, -0.5));

        VisualFunctionContact secondInputContact = circuit.getOrCreateContact(component, gate.in2, Contact.IOType.INPUT);
        secondInputContact.setPosition(new Point2D.Double(-1.5, 0.5));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, gate.out, Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        Contact firstVar = firstInputContact.getReferencedContact();
        Contact secondVar = secondInputContact.getReferencedContact();
        BooleanWorker worker = new CleverBooleanWorker();
        BooleanFormula setFunction = BooleanOperations.and(firstVar, secondVar, worker);
        outputContact.setSetFunction(setFunction);

        return component;
    }

    public static VisualFunctionComponent createOrGate(VisualCircuit circuit) {
        Gate3 gate = CircuitSettings.parseOrData();
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);
        component.setLabel(gate.name);

        VisualFunctionContact firstInputContact = circuit.getOrCreateContact(component, gate.in1, Contact.IOType.INPUT);
        firstInputContact.setPosition(new Point2D.Double(-1.5, -0.5));

        VisualFunctionContact secondInputContact = circuit.getOrCreateContact(component, gate.in2, Contact.IOType.INPUT);
        secondInputContact.setPosition(new Point2D.Double(-1.5, 0.5));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, gate.out, Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        Contact firstVar = firstInputContact.getReferencedContact();
        Contact secondVar = secondInputContact.getReferencedContact();
        BooleanWorker worker = new CleverBooleanWorker();
        BooleanFormula setFunction = BooleanOperations.or(firstVar, secondVar, worker);
        outputContact.setSetFunction(setFunction);

        return component;
    }

    public static VisualFunctionComponent createNandbGate(VisualCircuit circuit) {
        Gate3 gate = CircuitSettings.parseNandbData();
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);
        component.setLabel(gate.name);

        VisualFunctionContact firstInputContact = circuit.getOrCreateContact(component, gate.in1, Contact.IOType.INPUT);
        firstInputContact.setPosition(new Point2D.Double(-1.5, -0.5));

        VisualFunctionContact secondInputContact = circuit.getOrCreateContact(component, gate.in2, Contact.IOType.INPUT);
        secondInputContact.setPosition(new Point2D.Double(-1.5, 0.5));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, gate.out, Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        Contact firstVar = firstInputContact.getReferencedContact();
        Contact secondVar = secondInputContact.getReferencedContact();
        outputContact.setSetFunction(BooleanOperations.nandb(firstVar, secondVar, new CleverBooleanWorker()));

        return component;
    }

    public static VisualFunctionComponent createNorbGate(VisualCircuit circuit) {
        Gate3 gate = CircuitSettings.parseNorbData();
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);
        component.setLabel(gate.name);

        VisualFunctionContact firstInputContact = circuit.getOrCreateContact(component, gate.in1, Contact.IOType.INPUT);
        firstInputContact.setPosition(new Point2D.Double(-1.5, -0.5));

        VisualFunctionContact secondInputContact = circuit.getOrCreateContact(component, gate.in2, Contact.IOType.INPUT);
        secondInputContact.setPosition(new Point2D.Double(-1.5, 0.5));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, gate.out, Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        Contact firstVar = firstInputContact.getReferencedContact();
        Contact secondVar = secondInputContact.getReferencedContact();
        outputContact.setSetFunction(BooleanOperations.norb(firstVar, secondVar, new CleverBooleanWorker()));

        return component;
    }

    public static void propagateInitialState(VisualCircuit circuit, VisualFunctionComponent component) {
        propagateInitialState(circuit.getMathModel(), component.getReferencedComponent());
    }

    public static void propagateInitialState(Circuit circuit, FunctionComponent component) {
        Pair<List<BooleanVariable>, List<BooleanFormula>> variableAsignment = getVariableAssignment(circuit, component);
        for (FunctionContact output : component.getFunctionOutputs()) {
            BooleanFormula setFunction = BooleanUtils.replaceClever(output.getSetFunction(),
                    variableAsignment.getFirst(), variableAsignment.getSecond());

            boolean isOne = One.instance().equals(setFunction);
            output.setInitToOne(isOne);
        }
    }

    public static boolean isExcitedComponent(Circuit circuit, FunctionComponent component) {
        Pair<List<BooleanVariable>, List<BooleanFormula>> variableAsignment = getVariableAssignment(circuit, component);
        for (FunctionContact output : component.getFunctionOutputs()) {
            BooleanFormula setFunction = BooleanUtils.replaceClever(output.getSetFunction(),
                    variableAsignment.getFirst(), variableAsignment.getSecond());
            if ((setFunction != null) && (One.instance().equals(setFunction) != output.getInitToOne())) {
                BooleanFormula resetFunction = BooleanUtils.replaceClever(output.getResetFunction(),
                        variableAsignment.getFirst(), variableAsignment.getSecond());

                if ((resetFunction == null) || (One.instance().equals(resetFunction) == output.getInitToOne())) {
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
                BooleanFormula state = initToOne ^ inversion ? One.instance() : Zero.instance();
                values.add(state);
            }
        }
        return Pair.of(variables, values);
    }

    public static Set<BooleanVariable> getUsedPortVariables(Circuit circuit) {
        Set<BooleanVariable> result = new HashSet();
        for (Contact contact : circuit.getInputPorts()) {
            if (!(contact instanceof FunctionContact)) continue;
            FunctionContact inputPort = (FunctionContact) contact;
            result.add(inputPort);
            result.addAll(getUsedVariables(inputPort));
        }
        return result;
    }

    public static Set<BooleanVariable> getUsedVariables(FunctionComponent component) {
        Set<BooleanVariable> result = new HashSet();
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
            literals.addAll(setFunction.accept(new LiteralsExtractor()));
        }
        BooleanFormula resetFunction = contact.getResetFunction();
        if (resetFunction != null) {
            literals.addAll(resetFunction.accept(new LiteralsExtractor()));
        }
        return literals;
    }

    public static List<VisualFunctionContact> getOrderedInputs(VisualFunctionComponent component) {
        List<VisualFunctionContact> result = new LinkedList<>();
        if (component.getReferencedComponent() != null) {
            for (FunctionContact contact: getOrderedInputs(component.getReferencedComponent())) {
                VisualFunctionContact visualContact = component.getVisualContact(contact);
                if (visualContact != null) {
                    result.add(visualContact);
                }
            }
        }
        return result;
    }

    public static List<FunctionContact> getOrderedInputs(FunctionComponent component) {
        List<FunctionContact> result = new LinkedList<>();
        FunctionContact outputContact = component.getGateOutput();
        if (outputContact != null) {
            BooleanFormula setFunction = outputContact.getSetFunction();
            List<BooleanVariable> orderedLiterals = setFunction.accept(new LiteralsExtractor());
            for (BooleanVariable literal : orderedLiterals) {
                if (literal instanceof FunctionContact) {
                    FunctionContact contact = (FunctionContact) literal;
                    result.add(contact);
                }
            }
        }
        return result;
    }

}
