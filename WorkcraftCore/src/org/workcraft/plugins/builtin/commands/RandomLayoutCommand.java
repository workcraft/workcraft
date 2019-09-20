package org.workcraft.plugins.builtin.commands;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Random;

import org.workcraft.plugins.builtin.settings.RandomLayoutSettings;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.utils.Hierarchy;

public class RandomLayoutCommand extends AbstractLayoutCommand {

    private static final Random r = new Random();

    @Override
    public String getDisplayName() {
        return "Random";
    }

    @Override
    public void layout(VisualModel model) {
        Point2D start = new Point2D.Double(RandomLayoutSettings.getStartX(), RandomLayoutSettings.getStartY());
        Point2D range = new Point2D.Double(RandomLayoutSettings.getRangeX(), RandomLayoutSettings.getRangeY());
        setChildrenRandomPosition(model.getRoot(), start, range);
        setPolylineConnections(model.getRoot());
    }

    private void setChildrenRandomPosition(Container container, Point2D start, Point2D range) {
        for (Node node : container.getChildren()) {
            double x = start.getX() + r.nextDouble() * range.getX();
            double y = start.getY() + r.nextDouble() * range.getY();
            Point2D pos = new Point2D.Double(x, y);
            if (node instanceof VisualTransformableNode) {
                VisualTransformableNode transformableNode = (VisualTransformableNode) node;
                transformableNode.setRootSpacePosition(pos);
            }
            if (node instanceof Container) {
                Point2D childrenRange = new Point2D.Double(range.getX() / 2.0, range.getY() / 2.0);
                double childenX = pos.getX() - childrenRange.getX() / 2.0;
                double childenY = pos.getY() - childrenRange.getY() / 2.0;
                Point2D childrenStart = new Point2D.Double(childenX, childenY);
                setChildrenRandomPosition((Container) node, childrenStart, childrenRange);
            }
        }
    }

    private void setPolylineConnections(Container container) {
        Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(container, VisualConnection.class);
        for (VisualConnection connection: connections) {
            connection.setConnectionType(ConnectionType.POLYLINE);
            connection.getGraphic().setDefaultControlPoints();
        }
    }

}
