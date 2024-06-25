package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionUtils;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.circuit.*;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.stream.Collectors;

public final class SpaceUtils {

    private SpaceUtils() {
    }

    public static void makeSpaceAroundContact(VisualCircuit circuit, VisualContact contact, double space) {
        // Change connection scale mode to LOCK_RELATIVELY for cleaner relocation of components
        Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class);
        HashMap<VisualConnection, VisualConnection.ScaleMode> connectionToScaleModeMap
                = ConnectionUtils.replaceConnectionScaleMode(connections, VisualConnection.ScaleMode.NONE);

        SpaceUtils.offsetComponentsFromContact(circuit, contact, space + 1.0);
        CircuitUtils.detachJoint(circuit, contact, space);
        // Restore connection scale mode
        ConnectionUtils.restoreConnectionScaleMode(connectionToScaleModeMap);
    }

    private static void offsetComponentsFromContact(VisualCircuit circuit, VisualContact contact, double space) {
        double minSpace = getMinSpaceAroundContact(circuit, contact);
        if (minSpace > space) {
            return;
        }
        int dx = contact.getDirection().getGradientX();
        int dy = contact.getDirection().getGradientY();
        double x0 = contact.getRootSpaceX();
        double y0 = contact.getRootSpaceY();
        double xSpace = dx * (space - minSpace);
        double ySpace = dy * (space - minSpace);

        Collection<VisualTransformableNode> transformableNodes = Hierarchy.getDescendantsOfType(circuit.getRoot(),
                VisualTransformableNode.class, node -> !(node instanceof VisualPage)
                        && !((node instanceof VisualContact) && ((VisualContact) node).isPin()));

        for (VisualTransformableNode transformableNode : transformableNodes) {
            Rectangle2D bb = getBoundingBox(transformableNode);
            double x = transformableNode.getRootSpaceX();
            double xBorder = x + bb.getX() + ((dx < 0) ? bb.getWidth() : 0.0);
            if ((xBorder - x0) * dx > 0) {
                transformableNode.setRootSpaceX(x + xSpace);
            }
            double y = transformableNode.getRootSpaceY();
            double yBorder = y + bb.getY() + ((dy < 0) ? bb.getHeight() : 0.0);
            if ((yBorder - y0) * dy > 0) {
                transformableNode.setRootSpaceY(y + ySpace);
            }
        }
    }

    private static double getMinSpaceAroundContact(VisualCircuit circuit, VisualContact contact) {
        double result = Double.MAX_VALUE;
        int dx = contact.getDirection().getGradientX();
        int dy = contact.getDirection().getGradientY();
        double x0 = contact.getRootSpaceX();
        double y0 = contact.getRootSpaceY();
        Set<VisualNode> connectedNodes = new HashSet<>();
        connectedNodes.addAll(circuit.getPreset(contact));
        connectedNodes.addAll(circuit.getPostset(contact));
        for (Node connectedNode : connectedNodes) {
            if (connectedNode instanceof VisualTransformableNode) {
                VisualTransformableNode transformableNode = (VisualTransformableNode) connectedNode;
                Rectangle2D bb = getBoundingBox(transformableNode);
                double x = transformableNode.getRootSpaceX();
                double xBorder = x + bb.getX() + ((dx < 0) ? bb.getWidth() : 0.0);
                double xSpace = Math.abs(xBorder - x0);
                if (((xBorder - x0) * dx > 0) && (xSpace < result)) {
                    result = xSpace;
                }
                double y = transformableNode.getRootSpaceY();
                double yBorder = y + bb.getY() + ((dy < 0) ? bb.getHeight() : 0.0);
                double ySpace = Math.abs(yBorder - y0);
                if (((yBorder - y0) * dy > 0) && (ySpace < result)) {
                    result = ySpace;
                }
            }
        }
        return result;
    }

    private static Rectangle2D getBoundingBox(VisualTransformableNode transformableNode) {
        return (transformableNode instanceof VisualComponent)
                ? ((VisualComponent) transformableNode).getInternalBoundingBoxInLocalSpace()
                : transformableNode.getBoundingBoxInLocalSpace();
    }

    public static Point2D getOffsetContactPosition(VisualContact contact, double space) {
        double offset = contact.isPort() ? -space : space;
        VisualContact.Direction direction = contact.getDirection();
        double x = contact.getRootSpaceX() + offset * direction.getGradientX();
        double y = contact.getRootSpaceY() + offset * direction.getGradientY();
        return new Point2D.Double(x, y);
    }

    public static void positionPort(VisualCircuit circuit, VisualContact port, boolean alignToRight) {
        Collection<Touchable> nodes = new HashSet<>(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualConnection.class));
        for (VisualConnection connection : circuit.getConnections(port)) {
            nodes.remove(connection);
        }
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualCircuitComponent.class));
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualJoint.class));

        Rectangle2D modelBox = BoundingBoxHelper.mergeBoundingBoxes(nodes);
        double x = TransformHelper.snapP5(alignToRight ? modelBox.getMaxX() : modelBox.getMinX());
        double y = modelBox.getCenterY();
        if (port.isOutput()) {
            VisualContact driver = CircuitUtils.findDriver(circuit, port, false);
            if (driver != null) {
                y = driver.getRootSpaceY();
            }
        } else {
            Collection<VisualContact> driven = CircuitUtils.findDriven(circuit, port, false);
            if (driven.size() == 1) {
                y = driven.iterator().next().getRootSpaceY();
            } else if (!driven.isEmpty()) {
                Point2D p = MixUtils.middleRootspacePosition(driven);
                if (p != null) {
                    y = TransformHelper.snapP5(p.getY());
                }
            }
        }
        boolean done = false;
        while (!done) {
            done = true;
            for (VisualContact otherPort : circuit.getVisualPorts()) {
                if ((port != otherPort) && (otherPort.getRootSpacePosition().distance(x, y) < 0.1)) {
                    y = TransformHelper.snapP5(y + 0.5);
                    done = false;
                }
            }
        }
        port.setRootSpacePosition(new Point2D.Double(x, y));
    }

    public static void alignPortWithPin(VisualContact port, VisualContact pin, double dx) {
        if ((port != null) && (pin != null)) {
            Point2D pinPosition = pin.getRootSpacePosition();
            port.setRootSpacePosition(new Point2D.Double(pinPosition.getX() + dx, pinPosition.getY()));
        }
    }

    public static void alignPortWithPin(VisualCircuit circuit, VisualContact port, double dx) {
        if (port != null) {
            VisualContact pin = null;
            if (port.isOutput()) {
                pin = CircuitUtils.findDriver(circuit, port, false);
            } else {
                Collection<VisualContact> drivenContacts = CircuitUtils.findDriven(circuit, port, false);
                for (VisualContact drivenContact : drivenContacts) {
                    if ((pin == null) || (drivenContact.getRootSpaceY() < pin.getRootSpaceY())) {
                        pin = drivenContact;
                    }
                }
            }
            if (pin != null) {
                port.setRootSpacePosition(new Point2D.Double(pin.getRootSpaceX() + dx, pin.getRootSpaceY()));
            }
        }
    }

    public static List<VisualFunctionComponent> orderComponentsByPosition(List<VisualFunctionComponent> components) {
        return components.stream()
                .sorted((o1, o2) -> {
                    int xComparison = Double.compare(o2.getRootSpaceX(), o1.getRootSpaceX());
                    return xComparison == 0 ? Double.compare(o2.getRootSpaceY(), o1.getRootSpaceY()) : xComparison;
                })
                .collect(Collectors.toList());
    }

}
