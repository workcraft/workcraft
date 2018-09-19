package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.MixUtils;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.plugins.circuit.*;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.LinkedList;

public class GateUtils {

    public static void insertGateAfter(VisualCircuit circuit, VisualCircuitComponent component, VisualContact predContact) {
        Container container = (Container) predContact.getParent();
        // Step up in the hierarchy for a self-loop
        if (container instanceof VisualCircuitComponent) {
            container = (Container) container.getParent();
        }
        circuit.reparent(container, circuit, circuit.getRoot(), Arrays.asList(component));

        LinkedList<VisualComponent> succComponents = new LinkedList<>();
        for (Node succNode : circuit.getPostset(predContact)) {
            if (succNode instanceof VisualComponent) {
                succComponents.add((VisualComponent) succNode);
            }
        }
        Point2D predPoint = predContact.getRootSpacePosition();
        Point2D succPoint = MixUtils.middleRootspacePosition(succComponents);
        Point2D pos = MixUtils.middlePoint(Arrays.asList(predPoint, succPoint));
        component.setPosition(pos);

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
            VisualConnection connection = (VisualConnection) circuit.getConnection(predContact, succComponent);
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

        Node fromNode = connection.getFirst();
        Node toNode = connection.getSecond();
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
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);

        VisualFunctionContact inputContact = circuit.getOrCreateContact(component, "I", Contact.IOType.INPUT);
        inputContact.setPosition(new Point2D.Double(-1.5, 0.0));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, "O", Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));
        outputContact.setSetFunction(inputContact.getReferencedContact());

        return component;
    }

    public static VisualFunctionComponent createAndGate(VisualCircuit circuit) {
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);

        VisualFunctionContact firstInputContact = circuit.getOrCreateContact(component, "A", Contact.IOType.INPUT);
        firstInputContact.setPosition(new Point2D.Double(-1.5, -0.5));

        VisualFunctionContact secondInputContact = circuit.getOrCreateContact(component, "B", Contact.IOType.INPUT);
        secondInputContact.setPosition(new Point2D.Double(-1.5, 0.5));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, "O", Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        Contact firstVar = firstInputContact.getReferencedContact();
        Contact secondVar = secondInputContact.getReferencedContact();
        BooleanWorker worker = new CleverBooleanWorker();
        BooleanFormula setFunction = BooleanOperations.and(firstVar, secondVar, worker);
        outputContact.setSetFunction(setFunction);

        return component;
    }

    public static VisualFunctionComponent createOrGate(VisualCircuit circuit) {
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);

        VisualFunctionContact firstInputContact = circuit.getOrCreateContact(component, "A", Contact.IOType.INPUT);
        firstInputContact.setPosition(new Point2D.Double(-1.5, -0.5));

        VisualFunctionContact secondInputContact = circuit.getOrCreateContact(component, "B", Contact.IOType.INPUT);
        secondInputContact.setPosition(new Point2D.Double(-1.5, 0.5));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, "O", Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        Contact firstVar = firstInputContact.getReferencedContact();
        Contact secondVar = secondInputContact.getReferencedContact();
        BooleanWorker worker = new CleverBooleanWorker();
        BooleanFormula setFunction = BooleanOperations.or(firstVar, secondVar, worker);
        outputContact.setSetFunction(setFunction);

        return component;
    }

    public static VisualFunctionComponent createNandbGate(VisualCircuit circuit) {
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);

        VisualFunctionContact firstInputContact = circuit.getOrCreateContact(component, "AN", Contact.IOType.INPUT);
        firstInputContact.setPosition(new Point2D.Double(-1.5, -0.5));

        VisualFunctionContact secondInputContact = circuit.getOrCreateContact(component, "B", Contact.IOType.INPUT);
        secondInputContact.setPosition(new Point2D.Double(-1.5, 0.5));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, "ON", Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        Contact firstVar = firstInputContact.getReferencedContact();
        Contact secondVar = secondInputContact.getReferencedContact();
        BooleanWorker worker = new CleverBooleanWorker();
        BooleanFormula setFunction = BooleanOperations.not(BooleanOperations.and(BooleanOperations.not(firstVar), secondVar, worker), worker);
        outputContact.setSetFunction(setFunction);

        return component;
    }

    public static VisualFunctionComponent createNorbGate(VisualCircuit circuit) {
        VisualFunctionComponent component = circuit.createVisualComponent(new FunctionComponent(), VisualFunctionComponent.class);

        VisualFunctionContact firstInputContact = circuit.getOrCreateContact(component, "AN", Contact.IOType.INPUT);
        firstInputContact.setPosition(new Point2D.Double(-1.5, -0.5));

        VisualFunctionContact secondInputContact = circuit.getOrCreateContact(component, "B", Contact.IOType.INPUT);
        secondInputContact.setPosition(new Point2D.Double(-1.5, 0.5));

        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, "ON", Contact.IOType.OUTPUT);
        outputContact.setPosition(new Point2D.Double(1.5, 0.0));

        Contact firstVar = firstInputContact.getReferencedContact();
        Contact secondVar = secondInputContact.getReferencedContact();
        BooleanWorker worker = new CleverBooleanWorker();
        BooleanFormula setFunction = BooleanOperations.not(BooleanOperations.or(BooleanOperations.not(firstVar), secondVar, worker), worker);
        outputContact.setSetFunction(setFunction);

        return component;
    }

    public static void propagateInitialState(VisualCircuit circuit, VisualFunctionComponent component) {
        propagateInitialState(circuit.getMathModel(), component.getReferencedFunctionComponent());
    }

    public static void propagateInitialState(Circuit circuit, FunctionComponent component) {
        LinkedList<BooleanVariable> variables = new LinkedList<>();
        LinkedList<BooleanFormula> values = new LinkedList<>();
        for (FunctionContact input : component.getFunctionInputs()) {
            Contact driver = CircuitUtils.findDriver(circuit, input, false);
            if (driver != null) {
                variables.add(input);
                BooleanFormula initToOne = driver.getInitToOne() ? One.instance() : Zero.instance();
                values.add(initToOne);
            }
        }

        for (FunctionContact output : component.getFunctionOutputs()) {
            BooleanFormula setFunction = BooleanUtils.replaceClever(output.getSetFunction(), variables, values);
            boolean isOne = One.instance().equals(setFunction);
            output.setInitToOne(isOne);
        }
    }

}
