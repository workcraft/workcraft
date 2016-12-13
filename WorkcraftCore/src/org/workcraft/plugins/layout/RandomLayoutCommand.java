/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.layout;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Random;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.util.Hierarchy;

public class RandomLayoutCommand extends AbstractLayoutCommand {
    Random r = new Random();

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
