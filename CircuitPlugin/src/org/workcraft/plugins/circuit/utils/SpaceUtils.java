package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionUtils;
import org.workcraft.dom.visual.connections.ControlPoint;
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

        Collection<VisualTransformableNode> components = Hierarchy.getDescendantsOfType(circuit.getRoot(),
                VisualTransformableNode.class, node -> !(node instanceof VisualGroup)
                        && !(node instanceof VisualPage) && !(node instanceof ControlPoint)
                        && !((node instanceof VisualContact) && ((VisualContact) node).isPin()));

        for (VisualTransformableNode node : components) {
            Rectangle2D bb = node.getBoundingBoxInLocalSpace();
            double x = node.getRootSpaceX();
            double xBorder = x + bb.getX() + ((dx < 0) ? bb.getWidth() : 0.0);
            if ((xBorder - x0) * dx > 0) {
                node.setRootSpaceX(x + xSpace);
            }
            double y = node.getRootSpaceY();
            double yBorder = y + bb.getY() + ((dy < 0) ? bb.getHeight() : 0.0);
            if ((yBorder - y0) * dy > 0) {
                node.setRootSpaceY(y + ySpace);
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
            if (node instanceof VisualTransformableNode) {
                VisualTransformableNode transformableNode = (VisualTransformableNode) node;
                Rectangle2D bb = transformableNode.getBoundingBoxInLocalSpace();
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

    public static Point2D getOffsetContactPosition(VisualContact contact, double space) {
        double d = contact.isPort() ? -space : space;
        double x = contact.getRootSpaceX() + d * contact.getDirection().getGradientX();
        double y = contact.getRootSpaceY() + d * contact.getDirection().getGradientY();
        return new Point2D.Double(x, y);
    }


    public static void positionPort(VisualCircuit circuit, VisualFunctionContact port) {
        Collection<Touchable> nodes = new HashSet<>();
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualConnection.class));
        for (VisualConnection resetConnection : circuit.getConnections(port)) {
            nodes.remove(resetConnection);
        }
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualCircuitComponent.class));
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualJoint.class));

        Rectangle2D modelBox = BoundingBoxHelper.mergeBoundingBoxes(nodes);
        Collection<VisualContact> driven = CircuitUtils.findDriven(circuit, port, false);
        double x = modelBox.getMinX();
        double y = driven.isEmpty() ? modelBox.getCenterY() : MixUtils.middleRootspacePosition(driven).getY();
        Point2D.Double pos = new Point2D.Double(TransformHelper.snapP5(x), TransformHelper.snapP5(y));
        port.setRootSpacePosition(pos);

        VisualJoint joint = CircuitUtils.detachJoint(circuit, port);
        if (joint != null) {
            joint.setRootSpacePosition(getOffsetContactPosition(port, 0.5));
        }
    }
}
