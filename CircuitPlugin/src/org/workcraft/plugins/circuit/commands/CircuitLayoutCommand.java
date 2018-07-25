package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.routing.RouterClient;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.impl.Route;
import org.workcraft.plugins.circuit.routing.impl.Router;
import org.workcraft.plugins.circuit.routing.impl.RouterTask;
import org.workcraft.plugins.transform.StraightenConnectionTransformationCommand;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class CircuitLayoutCommand extends AbstractLayoutCommand {

    private static final double START_TO_JOINT_DISTANCE_THRESHOLD = 0.1;
    private static final double SAME_POINT_DISTANCE_THRESHOLD = 0.1;
    private static final double SAME_POINT_GRADIENT_THRESHOLD = 0.01;
    private static final double ALIGN_OFFSET_THRESHOLD = 0.5;
    private static final double ADJACENT_ALIGN_OFFSET_THRESHOLD = 0.01;

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
                setContactOrientation(circuit);
                setComponentPositions(circuit);
                alignBasicGates(circuit);
                alignPorts(circuit);
                distributeOverlappingInputPorts(circuit);
                distributeOverlappingOutputPorts(circuit);
            }
            prepareConnections(circuit);
            if (!skipLayoutRouting()) {
                routeWires(circuit);
            } else {
                routeSelfLoops(circuit);
            }
            // First filter, then align, and finally merge
            filterConnections(circuit);
            alignControlPoints(circuit);
            mergeConnections(circuit);
        }
    }

    public boolean skipLayoutPlacement() {
        return false;
    }

    public boolean skipLayoutRouting() {
        return false;
    }

    private void setContactOrientation(VisualCircuit circuit) {
        for (VisualContact port: circuit.getVisualPorts()) {
            port.setDefaultDirection();
        }

        for (VisualFunctionComponent component: circuit.getVisualFunctionComponents()) {
            for (VisualContact contact : component.getContacts()) {
                if (contact.isInput()) {
                    contact.setPosition(new Point2D.Double(-1.5, 0.0));
                } else {
                    contact.setPosition(new Point2D.Double(1.5, 0.0));
                }
            }
            component.setContactsDefaultPosition();
        }
    }

    private void setComponentPositions(VisualCircuit circuit) {
        double dx = CircuitLayoutSettings.getSpacingHorizontal();
        double dy = CircuitLayoutSettings.getSpacingVertical();
        LinkedList<HashSet<VisualComponent>> layers = filterBasicGates(circuit, rankComponents(circuit));
        // Calculate layer dimensions
        double totalWidth = 0.0;
        HashMap<HashSet<VisualComponent>, Point2D> layerSizes = new HashMap<>();
        for (HashSet<VisualComponent> layer: layers) {
            double layerWidth = 0.0;
            double layerHeight = 0.0;
            for (VisualComponent component: layer) {
                Rectangle2D bb = component.getBoundingBox();
                double width = bb.getWidth() + dx;
                if (width > layerWidth) {
                    layerWidth = width;
                }
                layerHeight += bb.getHeight() + dy;
            }
            totalWidth += layerWidth;
            Point2D layerSize = new Point2D.Double(layerWidth, layerHeight);
            layerSizes.put(layer, layerSize);
        }
        // Set positions of components in layers
        double x = -totalWidth / 2.0;
        for (HashSet<VisualComponent> layer: layers) {
            Point2D layerSize = layerSizes.get(layer);
            double layerWidth = (layerSize == null) ? 0.0 : layerSize.getX();
            x += layerWidth / 2.0;
            double layerHeight = (layerSize == null) ? 0.0 : layerSize.getY();
            double y = -layerHeight / 2.0;
            for (VisualComponent component: layer) {
                Rectangle2D bb = component.getBoundingBox();
                double sliceHeight = bb.getHeight() + dy;
                y += sliceHeight / 2.0;
                Point2D pos = new Point2D.Double(x, y);
                component.setPosition(pos);
                y += sliceHeight / 2.0;
            }
            x += layerWidth / 2.0;
        }
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
        for (VisualContact contact : Hierarchy.getDescendantsOfType(model.getRoot(), VisualContact.class)) {
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
            HashSet<VisualComponent> filteredLayer = new HashSet<>();
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
            if (filteredLayer.isEmpty()) {
                result.add(layer);
            } else {
                result.add(filteredLayer);
            }
        }
        return result;
    }

    private boolean isBasicGate(VisualCircuitComponent component) {
        return (component.getVisualOutputs().size() == 1) && (component.getVisualInputs().size() < 2);
    }

    private void prepareConnections(VisualCircuit circuit) {
        circuit.selectNone();
        // Dissolve joints
        DissolveJointTransformationCommand splitJointsCommand = new DissolveJointTransformationCommand();
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
        double xOffset = CircuitLayoutSettings.getSpacingHorizontal() / 4.0;
        if (xOffset < 2.5) {
            xOffset = 2.5;
        }
        for (VisualCircuitComponent component: Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualCircuitComponent.class)) {
            if (isBasicGate(component)) {
                VisualContact output = component.getFirstVisualOutput();
                Collection<VisualContact> drivens = CircuitUtils.findDriven(circuit, output);
                if (drivens.size() == 1) {
                    VisualContact driven = drivens.iterator().next();
                    double x = driven.getRootSpaceX();
                    double y = driven.getRootSpaceY();
//                    if (driven.isPort()) {
//                        VisualContact input = component.getFirstVisualInput();
//                        VisualContact driver = CircuitUtils.findDriver(circuit, input);
//                        if (driven != null) {
//                            y = driver.getRootSpaceY();
//                        }
//                    }
                    component.setRootSpacePosition(new Point2D.Double(x - xOffset, y));
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

    private void distributeOverlappingInputPorts(VisualCircuit circuit) {
        boolean done = false;
        while (!done) {
            done = true;
            for (VisualContact contact1: circuit.getVisualPorts()) {
                if (!contact1.isInput()) continue;
                double y = contact1.getRootSpaceY();
                for (VisualContact contact2: circuit.getVisualPorts()) {
                    if (!contact2.isInput()) continue;
                    if (contact2 == contact1) continue;
                    if (Math.abs(contact2.getRootSpaceY() - y) > 0.5) continue;
                    contact2.setRootSpaceY(contact1.getRootSpaceY() + 1.0);
                    done = false;
                }
            }
        }
    }

    private void distributeOverlappingOutputPorts(VisualCircuit circuit) {
        boolean done = false;
        while (!done) {
            done = true;
            for (VisualContact contact1: circuit.getVisualPorts()) {
                if (!contact1.isOutput()) continue;
                double y = contact1.getRootSpaceY();
                for (VisualContact contact2: circuit.getVisualPorts()) {
                    if (!contact2.isOutput()) continue;
                    if (contact2 == contact1) continue;
                    if (Math.abs(contact2.getRootSpaceY() - y) > 0.5) continue;
                    contact2.setRootSpaceY(y + 1.0);
                    done = false;
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

    private void filterConnections(VisualCircuit circuit) {
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class)) {
            ConnectionGraphic graphic = connection.getGraphic();
            if (graphic instanceof Polyline) {
                ConnectionHelper.filterControlPoints((Polyline) graphic,
                        SAME_POINT_DISTANCE_THRESHOLD, SAME_POINT_GRADIENT_THRESHOLD);
            }
        }
    }

    private void mergeConnections(VisualCircuit circuit) {
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
            Point2D commonPos = getLastCommonPointInRootSpace(c1, c2);
            if ((commonPos != null) && (startPos.distance(commonPos) > START_TO_JOINT_DISTANCE_THRESHOLD)) {
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
            if (commonPos.distance(joint.getRootSpacePosition()) < SAME_POINT_DISTANCE_THRESHOLD) {
                return joint;
            }
        }
        return null;
    }

    private Point2D getLastCommonPointInRootSpace(VisualConnection c1, VisualConnection c2) {
        if (c1.getFirst() != c2.getFirst()) {
            return null;
        }
        Point2D pos = ((VisualTransformableNode) c1.getFirst()).getRootSpacePosition();
        ConnectionGraphic g1 = c1.getGraphic();
        ConnectionGraphic g2 = c2.getGraphic();
        if ((g1 instanceof Polyline) && (g2 instanceof Polyline)) {
            Polyline poly1 = (Polyline) g1;
            Polyline poly2 = (Polyline) g2;
            int count = Math.min(poly1.getControlPointCount(), poly2.getControlPointCount());
            for (int i = 0; i <= count; i++) {
                Point2D pos1 = (i < poly1.getControlPointCount()) ? poly1.getControlPoint(i).getRootSpacePosition()
                                       : ((VisualTransformableNode) c1.getSecond()).getRootSpacePosition();
                Point2D pos2 = (i < poly2.getControlPointCount()) ? poly2.getControlPoint(i).getRootSpacePosition()
                                       : ((VisualTransformableNode) c2.getSecond()).getRootSpacePosition();
                if (pos1.distance(pos2) < SAME_POINT_DISTANCE_THRESHOLD) {
                    pos = pos1;
                } else {
                    double gradient = ConnectionHelper.calcGradient(pos, pos1, pos2);
                    boolean sameSide = ((pos2.getX() > pos.getX()) == (pos1.getX() > pos.getX()))
                                               && ((pos2.getY() > pos.getY()) == (pos1.getY() > pos.getY()));
                    if ((Math.abs(gradient) < SAME_POINT_GRADIENT_THRESHOLD) && sameSide) {
                        pos = (pos.distanceSq(pos1) < pos.distanceSq(pos2)) ? pos1 : pos2;
                    }
                    break;
                }
            }
        }
        return pos;
    }

    private void appendConnection(VisualCircuit circuit, VisualJoint joint, VisualConnection connection) {
        Point2D pInRootSpace = joint.getRootSpacePosition();
        AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(connection);
        Point2D pInLocalSpace = rootToLocalTransform.transform(pInRootSpace, null);
        LinkedList<Point2D> suffixLocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection, pInLocalSpace);
        circuit.remove(connection);
        try {
            VisualConnection succConnection = circuit.connect(joint, connection.getSecond());
            ConnectionHelper.addControlPoints(succConnection, suffixLocationsInRootSpace);
            succConnection.copyStyle(connection);
        } catch (InvalidConnectionException e) {
        }
    }

    private void mergeCommonConnectionSegment(VisualCircuit circuit, VisualConnection c1, VisualConnection c2, Point2D pInRootSpace) {
        AffineTransform rootToLocalTransform = TransformHelper.getTransformFromRoot(c1);
        Point2D pInLocalSpace = rootToLocalTransform.transform(pInRootSpace, null);
        LinkedList<Point2D> commonLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(c1, pInLocalSpace);
        LinkedList<Point2D> c1LocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(c1, pInLocalSpace);
        LinkedList<Point2D> c2LocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(c2, pInLocalSpace);

        Container container = Hierarchy.getNearestContainer(c1, c2);
        VisualJoint joint = circuit.createJoint(container);
        joint.setRootSpacePosition(pInRootSpace);
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

    private void alignControlPoints(VisualCircuit circuit) {
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class)) {
            ConnectionGraphic graphic = connection.getGraphic();
            if (graphic instanceof Polyline) {
                Polyline polyline = (Polyline) graphic;
                AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);

                ControlPoint fcp = polyline.getFirstControlPoint();
                Point2D fcpnPos = polyline.getNextAnchorPointLocation(fcp);
                alignControlPointToNode(fcp, connection.getFirst(), fcpnPos, localToRootTransform);

                ControlPoint lcp = polyline.getLastControlPoint();
                Point2D lcpnPos = polyline.getPrevAnchorPointLocation(lcp);
                alignControlPointToNode(lcp, connection.getSecond(), lcpnPos, localToRootTransform);
            }
        }
    }

    private void alignControlPointToNode(ControlPoint cp, VisualNode node, Point2D adjacentPos, AffineTransform localToRootTransform) {
        if ((cp != null) && (node instanceof  VisualTransformableNode) && (adjacentPos != null)) {
            double x = ((VisualTransformableNode) node).getRootSpaceX();
            double y = ((VisualTransformableNode) node).getRootSpaceY();
            Point2D adjacentPosInRootSpace = localToRootTransform.transform(adjacentPos, null);
            if ((Math.abs(cp.getRootSpaceX() - adjacentPosInRootSpace.getX()) < ADJACENT_ALIGN_OFFSET_THRESHOLD)
                    && (Math.abs(cp.getRootSpaceY() - y) < ALIGN_OFFSET_THRESHOLD)) {
                cp.setRootSpaceY(y);
            } else if ((Math.abs(cp.getRootSpaceY() - adjacentPosInRootSpace.getY()) < ADJACENT_ALIGN_OFFSET_THRESHOLD)
                    && (Math.abs(cp.getRootSpaceX() - x) < ALIGN_OFFSET_THRESHOLD)) {
                cp.setRootSpaceX(x);
            }
        }
    }

}
