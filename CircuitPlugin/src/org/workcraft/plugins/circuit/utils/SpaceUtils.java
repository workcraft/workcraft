package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.util.Hierarchy;

import java.awt.geom.Rectangle2D;

public class SpaceUtils {

    public static void makeSpaceAfterContact(VisualCircuit circuit, VisualContact contact, double space) {
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
        Container container = (Container) contact.getParent();
        if (container instanceof VisualCircuitComponent) {
            container = (Container) container.getParent();
        }
        for (VisualTransformableNode node : Hierarchy.getChildrenOfType(container, VisualTransformableNode.class)) {
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

}
