package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionUtils;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.circuit.*;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class SpaceUtils {

    public static void makeSpaceAfterContact(VisualCircuit circuit, VisualContact contact, double space) {
        // Change connection scale mode to LOCK_RELATIVELY for cleaner relocation of components
        Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class);
        HashMap<VisualConnection, VisualConnection.ScaleMode> connectionToScaleModeMap
                = ConnectionUtils.replaceConnectionScaleMode(connections, VisualConnection.ScaleMode.LOCK_RELATIVELY);

        SpaceUtils.offsetComponentsFromContact(circuit, contact, space + 1.0);
        VisualJoint joint = CircuitUtils.detachJoint(circuit, contact);
        if (joint != null) {
            joint.setRootSpacePosition(getOffsetContactPosition(contact, space));
        }
        // Restore connection scale mode
        ConnectionUtils.restoreConnectionScaleMode(connectionToScaleModeMap);
    }

    public static void offsetComponentsFromContact(VisualCircuit circuit, VisualContact contact, double space) {
        double minSpace = getMinSpace(circuit, contact);
        if (minSpace > space) {
            return;
        }
        int dx = contact.getDirection().getGradientX();
        int dy = contact.getDirection().getGradientY();
        double x0 = contact.getRootSpaceX();
        double y0 = contact.getRootSpaceY();
        double xSpace = dx * (space - minSpace);
        double ySpace = dy * (space - minSpace);

        Collection<VisualComponent> components = Hierarchy.getDescendantsOfType(circuit.getRoot(),
                VisualComponent.class, node -> !(node instanceof VisualPage)
                        && !((node instanceof VisualContact) && ((VisualContact) node).isPin()));

        for (VisualComponent component : components) {
            Rectangle2D bb = component.getInternalBoundingBoxInLocalSpace();
            double x = component.getRootSpaceX();
            double xBorder = x + bb.getX() + ((dx < 0) ? bb.getWidth() : 0.0);
            if ((xBorder - x0) * dx > 0) {
                component.setRootSpaceX(x + xSpace);
            }
            double y = component.getRootSpaceY();
            double yBorder = y + bb.getY() + ((dy < 0) ? bb.getHeight() : 0.0);
            if ((yBorder - y0) * dy > 0) {
                component.setRootSpaceY(y + ySpace);
            }
        }
    }

    private static double getMinSpace(VisualCircuit circuit, VisualContact contact) {
        double result = Double.MAX_VALUE;
        int dx = contact.getDirection().getGradientX();
        int dy = contact.getDirection().getGradientY();
        double x0 = contact.getRootSpaceX();
        double y0 = contact.getRootSpaceY();
        for (Node node : circuit.getPostset(contact)) {
            if (node instanceof VisualComponent) {
                VisualComponent component = (VisualComponent) node;
                Rectangle2D bb = component.getInternalBoundingBoxInLocalSpace();
                double x = component.getRootSpaceX();
                double xBorder = x + bb.getX() + ((dx < 0) ? bb.getWidth() : 0.0);
                double xSpace = Math.abs(xBorder - x0);
                if (((xBorder - x0) * dx > 0) && (xSpace < result)) {
                    result = xSpace;
                }
                double y = component.getRootSpaceY();
                double yBorder = y + bb.getY() + ((dy < 0) ? bb.getHeight() : 0.0);
                double ySpace = Math.abs(yBorder - y0);
                if (((yBorder - y0) * dy > 0) && (ySpace < result)) {
                    result = ySpace;
                }
            }
        }
        return result;
    }

    public static Point2D getOffsetContactPosition(VisualContact contact, double space) {
        double d = contact.isPort() ? -space : space;
        double x = contact.getRootSpaceX() + d * contact.getDirection().getGradientX();
        double y = contact.getRootSpaceY() + d * contact.getDirection().getGradientY();
        return new Point2D.Double(x, y);
    }

    public static void positionPort(VisualCircuit circuit, VisualFunctionContact port, boolean alignToRight) {
        Collection<Touchable> nodes = new HashSet<>();
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualConnection.class));
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
                y = TransformHelper.snapP5(MixUtils.middleRootspacePosition(driven).getY());
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

        if (port.isInput()) {
            VisualJoint joint = CircuitUtils.detachJoint(circuit, port);
            if (joint != null) {
                joint.setRootSpacePosition(getOffsetContactPosition(port, 0.5));
            }
        }
    }
}
