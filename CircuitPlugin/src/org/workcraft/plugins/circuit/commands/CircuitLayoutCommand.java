package org.workcraft.plugins.circuit.commands;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.commands.AbstractLayoutCommand;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.routing.RouterClient;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.impl.Route;
import org.workcraft.plugins.circuit.routing.impl.Router;
import org.workcraft.plugins.circuit.routing.impl.RouterTask;
import org.workcraft.plugins.transform.StraightenConnectionTransformationCommand;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitLayoutCommand extends AbstractLayoutCommand {

    @Override
    public String getDisplayName() {
        return "Circuit placement and routing";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public void layout(VisualModel model) {
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            if (!skipLayoutPlacement()) {
                setComponentPosition(circuit);
                alignBasicGates(circuit);
                alignPorts(circuit);
                distributeOverlappingPorts(circuit);
            }
            prepareConnections(circuit);
            if (!skipLayoutRouting()) {
                routeWires(circuit);
            } else {
                routeSelfLoops(circuit);
            }
            mergeConnections(circuit);
        }
    }

    public boolean skipLayoutPlacement() {
        return false;
    }

    public boolean skipLayoutRouting() {
        return false;
    }

    private void setComponentPosition(VisualCircuit circuit) {
        double dx = CircuitLayoutSettings.getSpacingHorizontal();
        double dy = CircuitLayoutSettings.getSpacingVertical();
        LinkedList<HashSet<VisualComponent>> layers = filterBasicGates(circuit, rankComponents(circuit));
        double x = (1.0 - layers.size()) * dx / 2.0;
        for (HashSet<VisualComponent> layer: layers) {
            double y = (1.0 - layer.size()) * dy / 2.0;
            for (VisualComponent component: layer) {
                double xOffset = (component instanceof VisualContact) ? 3.0 : 0.0;
                Point2D pos = new Point2D.Double(x - xOffset, y);
                component.setPosition(pos);
                if (component instanceof VisualCircuitComponent) {
                    VisualCircuitComponent circuitComponent = (VisualCircuitComponent) component;
                    setContactPositions(circuitComponent);
                }
                y += dy;
            }
            x += dx;
        }
    }

    private void setContactPositions(VisualCircuitComponent circuitComponent) {
        for (VisualContact contact: circuitComponent.getContacts()) {
            if (contact.isInput()) {
                contact.setPosition(new Point2D.Double(-1.5, 0.0));
            } else {
                contact.setPosition(new Point2D.Double(1.5, 0.0));
            }
        }
        circuitComponent.setContactsDefaultPosition();
    }

    private LinkedList<HashSet<VisualComponent>> rankComponents(VisualCircuit model) {
        LinkedList<HashSet<VisualComponent>> result = new LinkedList<>();

        HashSet<VisualComponent> inputPorts = new HashSet<>();
        for (VisualContact contact: Hierarchy.getDescendantsOfType(model.getRoot(), VisualContact.class)) {
            if (contact.isPort() && contact.isInput()) {
                inputPorts.add(contact);
            }
        }

        HashSet<VisualCircuitComponent> remainingComponents = new HashSet<>(Hierarchy.getDescendantsOfType(
                model.getRoot(), VisualCircuitComponent.class));

        HashSet<VisualComponent> currentLayer = inputPorts;
        HashSet<VisualComponent> firstLayer = null;
        while (!currentLayer.isEmpty()) {
            remainingComponents.removeAll(currentLayer);
            result.add(currentLayer);
            currentLayer = getNextLayer(model, currentLayer);
            currentLayer.retainAll(remainingComponents);
            if (firstLayer == null) {
                firstLayer = currentLayer;
            }
        }
        if (firstLayer == null) {
            firstLayer = new HashSet<>();
        }
        firstLayer.addAll(remainingComponents);
        if ((result.size() < 2) && !firstLayer.isEmpty()) {
            result.add(firstLayer);
        }

        HashSet<VisualComponent> outputPorts = new HashSet<>();
        for (VisualContact contact: Hierarchy.getDescendantsOfType(model.getRoot(), VisualContact.class)) {
            if (contact.isPort() && contact.isOutput()) {
                outputPorts.add(contact);
            }
        }
        result.add(outputPorts);
        return result;
    }

    private HashSet<VisualComponent> getNextLayer(final VisualCircuit model, HashSet<VisualComponent> layer) {
        HashSet<VisualComponent> result = new HashSet<>();
        for (VisualComponent component: layer) {
            result.addAll(CircuitUtils.getComponentPostset(model, component));
        }
        return result;
    }

    private LinkedList<HashSet<VisualComponent>> filterBasicGates(VisualCircuit circuit, LinkedList<HashSet<VisualComponent>> layers) {
        LinkedList<HashSet<VisualComponent>> result = new LinkedList<>();
        for (HashSet<VisualComponent> layer: layers) {
            HashSet<VisualComponent> filteredLayer = new HashSet<VisualComponent>();
            for (VisualComponent component: layer) {
                if (component instanceof VisualContact) {
                    filteredLayer.add(component);
                } else if (component instanceof VisualCircuitComponent) {
                    VisualCircuitComponent circuitComponent = (VisualCircuitComponent) component;
                    if (!isBasicGate(circuitComponent)) {
                        filteredLayer.add(component);
                    } else {
                        VisualContact output = circuitComponent.getFirstVisualOutput();
                        Collection<VisualContact> driven = CircuitUtils.findDriven(circuit, output);
                        if (driven.size() != 1) {
                            filteredLayer.add(component);
                        }
                    }
                }
            }
            if (!filteredLayer.isEmpty()) {
                result.add(filteredLayer);
            }
        }
        return result;
    }

    private boolean isBasicGate(VisualCircuitComponent component) {
        return (component.getVisualOutputs().size() == 1) && (component.getVisualInputs().size() == 1);
    }

    private void prepareConnections(VisualCircuit circuit) {
        circuit.selectNone();
        // Split joints
        SplitJointTransformationCommand splitJointsCommand = new SplitJointTransformationCommand();
        Collection<Node> joints = splitJointsCommand.collect(circuit);
        splitJointsCommand.transform(circuit, joints);
        // Straighten connections
        StraightenConnectionTransformationCommand straightenConnectionsCommand = new StraightenConnectionTransformationCommand();
        Collection<Node> connections = straightenConnectionsCommand.collect(circuit);
        straightenConnectionsCommand.transform(circuit, connections);
    }

    private void routeSelfLoops(VisualCircuit circuit) {
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class)) {
            VisualNode firstNode = connection.getFirst();
            VisualNode secondNode = connection.getSecond();
            if ((firstNode instanceof VisualContact) && (secondNode instanceof VisualContact)) {
                VisualContact firstContact = (VisualContact) firstNode;
                VisualContact secondContact = (VisualContact) secondNode;
                if (!firstContact.isPort() && !secondContact.isPort()
                        && (firstContact.getParent() == secondContact.getParent())) {
                    Point2D firstPos = firstContact.getRootSpacePosition();
                    Point2D secondPos = secondContact.getRootSpacePosition();
                    Node parent = firstContact.getParent();
                    double h = 2.0;
                    if (parent instanceof VisualCircuitComponent) {
                        VisualCircuitComponent component = (VisualCircuitComponent) parent;
                        Rectangle2D bb = component.getInternalBoundingBoxInLocalSpace();
                        h = bb.getHeight();
                    }
                    double d = firstPos.getY() - secondPos.getY();
                    double dx = 1.0 - Math.abs(d);
                    if (dx < 0.0) dx = 0.0;
                    double dy = (d > 0) ? h - d : -h - d;
                    ConnectionGraphic graphic = connection.getGraphic();
                    if (graphic instanceof Polyline) {
                        Polyline polyline = (Polyline) graphic;
                        polyline.addControlPoint(new Point2D.Double(firstPos.getX(), firstPos.getY() - dy));
                        polyline.addControlPoint(new Point2D.Double(secondPos.getX() - dx, firstPos.getY() - dy));
                        polyline.addControlPoint(new Point2D.Double(secondPos.getX() - dx, secondPos.getY()));
                    }
                }
            }
        }
    }

    private void alignBasicGates(VisualCircuit circuit) {
        for (VisualCircuitComponent component: Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualCircuitComponent.class)) {
            if (isBasicGate(component)) {
                VisualContact output = component.getFirstVisualOutput();
                Collection<VisualContact> drivens = CircuitUtils.findDriven(circuit, output);
                if (drivens.size() == 1) {
                    VisualContact driven = drivens.iterator().next();
                    double x = driven.getRootSpaceX();
                    double y = driven.getRootSpaceY();
                    if (driven.isPort()) {
                        VisualContact input = component.getFirstVisualInput();
                        VisualContact driver = CircuitUtils.findDriver(circuit, input);
                        if (driven != null) {
                            y = driver.getRootSpaceY();
                        }
                    }
                    component.setRootSpacePosition(new Point2D.Double(x - 2.5, y));
                    setContactPositions(component);
                }
            }
        }
    }

    private void alignPorts(VisualCircuit circuit) {
        for (VisualContact contact: circuit.getVisualPorts()) {
            if (contact.isOutput()) {
                VisualContact driver = CircuitUtils.findDriver(circuit, contact);
                if (driver != null) {
                    double y = driver.getRootSpaceY();
                    contact.setRootSpaceY(y);
                }
            }
            if (contact.isInput()) {
                double y = 0.0;
                int count = 0;
                for (VisualContact driven: CircuitUtils.findDriven(circuit, contact)) {
                    y += driven.getRootSpaceY();
                    count++;
                }
                if (count > 0) {
                    contact.setRootSpaceY(y / (double) count);
                }
            }
        }
    }

    private void distributeOverlappingPorts(VisualCircuit circuit) {
        double dy2 = CircuitLayoutSettings.getSpacingVertical() / 2.0;
        // Distribute overlapping output ports.
        for (VisualContact contact1: circuit.getVisualPorts()) {
            if (!contact1.isOutput()) continue;
            double y = contact1.getRootSpaceY();
            for (VisualContact contact2: circuit.getVisualPorts()) {
                if (!contact2.isOutput()) continue;
                if (contact2 == contact1) continue;
                if (Math.abs(contact2.getRootSpaceY() - y) < 0.01) {
                    VisualContact driver1 = CircuitUtils.findDriver(circuit, contact1);
                    VisualContact driver2 = CircuitUtils.findDriver(circuit, contact2);
                    if (driver1.getRootSpaceX() < driver2.getRootSpaceX()) {
                        contact1.setRootSpaceY(y - dy2);
                    } else {
                        contact2.setRootSpaceY(y - dy2);
                    }
                }
            }
        }
        // Distribute overlapping input ports.
        for (VisualContact contact1: circuit.getVisualPorts()) {
            if (!contact1.isInput()) continue;
            double y = contact1.getRootSpaceY();
            for (VisualContact contact2: circuit.getVisualPorts()) {
                if (!contact2.isInput()) continue;
                if (contact2 == contact1) continue;
                if (Math.abs(contact2.getRootSpaceY() - y) < 0.01) {
                    Collection<VisualContact> driven1 = CircuitUtils.findDriven(circuit, contact1);
                    if (driven1.size() > 1) {
                        contact1.setRootSpaceY(y - dy2);
                    } else {
                        contact2.setRootSpaceY(y - dy2);
                    }
                }
            }
        }
    }

    private void routeWires(VisualCircuit circuit) {
        Router router = new Router();
        RouterClient routingClient = new RouterClient();
        RouterTask routerTask = routingClient.registerObstacles(circuit);
        router.routeConnections(routerTask);
        for (Route route: router.getRoutingResult()) {
            VisualContact srcContact = routingClient.getContact(route.source);
            VisualContact dstContact = routingClient.getContact(route.destination);
            Connection connection = circuit.getConnection(srcContact, dstContact);
            if (connection instanceof VisualConnection) {
                List<Point2D> locationsInRootSpace = new ArrayList<>();
                for (Point routePoint: route.getPoints()) {
                    locationsInRootSpace.add(new Point2D.Double(routePoint.getX(), routePoint.getY()));
                }
                ConnectionHelper.addControlPoints((VisualConnection) connection, locationsInRootSpace);
            }
        }
    }

    private void mergeConnections(VisualCircuit circuit) {
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class)) {
            ConnectionGraphic grapic = connection.getGraphic();
            if (grapic instanceof Polyline) {
                ConnectionHelper.filterControlPoints((Polyline) grapic, 0.01, 0.01);
            }
        }
        boolean progress = true;
        while (progress) {
            progress = false;
            Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class);
            for (VisualConnection c1: connections) {
                for (VisualConnection c2: connections) {
                    if (mergeConnections(circuit, c1, c2)) {
                        progress = true;
                        break;
                    }
                }
                if (progress) break;
            }
        }
    }

    private boolean mergeConnections(VisualCircuit circuit, VisualConnection c1, VisualConnection c2) {
        if ((c1 != c2) && (c1.getFirst() == c2.getFirst())) {
            VisualTransformableNode src = (VisualTransformableNode) c1.getFirst();
            Point2D startPos = src.getRootSpacePosition();
            Point2D commonPos = getLastCommonPoint(c1, c2);
            //printConnectionPoints(c1, c2, p);
            if ((commonPos != null) && (startPos.distanceSq(commonPos) > 0.01)) {
                VisualJoint j1 = getCommonJoint(c1, commonPos);
                VisualJoint j2 = getCommonJoint(c2, commonPos);
                if ((j1 == null) && (j2 == null)) {
                    mergeCommonConnectionSegment(circuit, c1, c2, commonPos);
                }
                if ((j1 != null) && (j2 == null)) {
                    appendConnection(circuit, j1, c2);
                }
                if ((j1 == null) && (j2 != null)) {
                    appendConnection(circuit, j2, c1);
                }
                if ((j1 != null) && (j2 != null)) {
                    mergeJoints(circuit, j1, j2);
                }
                return true;
            }
        }
        return false;
    }

    private VisualJoint getCommonJoint(VisualConnection connection, Point2D commonPos) {
        if (connection.getSecond() instanceof VisualJoint) {
            VisualJoint joint = (VisualJoint) connection.getSecond();
            if (commonPos.distanceSq(joint.getRootSpacePosition()) < 0.01) {
                return joint;
            }
        }
        return null;
    }

    private void printConnectionPoints(VisualConnection c1, VisualConnection c2, Point2D commonPos) {
        for (VisualConnection connection: new VisualConnection[]{c1, c2}) {
            System.out.print(connection);
            System.out.print(" = { " + connection.getFirstCenter());
            ConnectionGraphic graphic = connection.getGraphic();
            if (graphic instanceof Polyline) {
                Polyline polyline = (Polyline) graphic;
                for (ControlPoint cp: polyline.getControlPoints()) {
                    System.out.print(", " + cp.getCenter());
                }
            }
            System.out.println(", " + connection.getSecondCenter() + " }");
        }
        System.out.println("Last common point: " + commonPos);
    }

    private Point2D getLastCommonPoint(VisualConnection c1, VisualConnection c2) {
        if (c1.getFirst() != c2.getFirst()) {
            return null;
        }
        Point2D pos = ((VisualTransformableNode) c1.getFirst()).getRootSpacePosition();
        ConnectionGraphic g1 = c1.getGraphic();
        ConnectionGraphic g2 = c2.getGraphic();
        if ((g1 instanceof Polyline) && (g2 instanceof Polyline)) {
            Polyline p1 = (Polyline) g1;
            Polyline p2 = (Polyline) g2;
            int count = Math.min(p1.getControlPointCount(), p2.getControlPointCount());
            for (int i = 0; i <= count; i++) {
                Point2D pos1 = (i < p1.getControlPointCount()) ? p1.getControlPoint(i).getRootSpacePosition()
                        : ((VisualTransformableNode) c1.getSecond()).getRootSpacePosition();
                Point2D pos2 = (i < p2.getControlPointCount()) ? p2.getControlPoint(i).getRootSpacePosition()
                        : ((VisualTransformableNode) c2.getSecond()).getRootSpacePosition();
                if (pos1.distanceSq(pos2) < 0.01) {
                    pos = pos1;
                } else {
                    double gradient = ConnectionHelper.calcGradient(pos, pos1, pos2);
                    boolean sameSide = ((pos2.getX() > pos.getX()) == (pos1.getX() > pos.getX()))
                            && ((pos2.getY() > pos.getY()) == (pos1.getY() > pos.getY()));
                    if ((Math.abs(gradient) < 0.01) && sameSide) {
                        pos = (pos.distanceSq(pos1) < pos.distanceSq(pos2)) ? pos1 : pos2;
                    }
                    break;
                }
            }
        }
        return pos;
    }

    private void appendConnection(VisualCircuit circuit, VisualJoint joint, VisualConnection connection) {
        Point2D p = joint.getRootSpacePosition();
        LinkedList<Point2D> suffixLocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection, p);
        circuit.remove(connection);
        try {
            VisualConnection succConnection = circuit.connect(joint, connection.getSecond());
            ConnectionHelper.addControlPoints(succConnection, suffixLocationsInRootSpace);
            succConnection.copyStyle(connection);
        } catch (InvalidConnectionException e) {
        }
    }

    private void mergeCommonConnectionSegment(VisualCircuit circuit, VisualConnection c1, VisualConnection c2, Point2D p) {
        LinkedList<Point2D> commonLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(c1, p);
        LinkedList<Point2D> c1LocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(c1, p);
        LinkedList<Point2D> c2LocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(c2, p);

        Container container = Hierarchy.getNearestContainer(c1, c2);
        VisualJoint joint = circuit.createJoint(container);
        joint.setPosition(p);
        circuit.remove(c1);
        circuit.remove(c2);

        try {
            VisualConnection commonConnection = circuit.connect(c1.getFirst(), joint);
            commonConnection.mixStyle(c1, c2);
            ConnectionHelper.addControlPoints(commonConnection, commonLocationsInRootSpace);

            VisualConnection succ1Connection = circuit.connect(joint, c1.getSecond());
            ConnectionHelper.addControlPoints(succ1Connection, c1LocationsInRootSpace);
            succ1Connection.copyStyle(c1);

            VisualConnection succ2Connection = circuit.connect(joint, c2.getSecond());
            ConnectionHelper.addControlPoints(succ2Connection, c2LocationsInRootSpace);
            succ2Connection.copyStyle(c2);
        } catch (InvalidConnectionException e) {
        }
    }

    private void mergeJoints(VisualCircuit circuit, VisualJoint j1, VisualJoint j2) {
        Set<VisualConnection> connections = new HashSet<>();
        for (Connection connection: circuit.getConnections(j1)) {
            if ((connection instanceof VisualConnection) && (connection.getFirst() == j1)) {
                connections.add((VisualConnection) connection);
            }
        }
        circuit.remove(j1);
        for (VisualConnection connection: connections) {
            try {
                VisualConnection newConnection = circuit.connect(j2, connection.getSecond());
                newConnection.copyShape(connection);
                newConnection.copyStyle(connection);
            } catch (InvalidConnectionException e) {
            }
        }
    }

}
