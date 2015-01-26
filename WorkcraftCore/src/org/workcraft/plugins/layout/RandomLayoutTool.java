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

import java.util.Collection;
import java.util.Random;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.util.Hierarchy;

public class RandomLayoutTool extends AbstractLayoutTool {
	Random r = new Random();

	@Override
	public String getDisplayName() {
		return "Random";
	}

	@Override
	public void layout(VisualModel model) {
		Collection<VisualTransformableNode> nodes = Hierarchy.getDescendantsOfType(model.getRoot(), VisualTransformableNode.class);
		for (VisualTransformableNode node : nodes) {
			node.setX(RandomLayoutSettings.getStartX() + r.nextDouble() * RandomLayoutSettings.getRangeX());
			node.setY(RandomLayoutSettings.getStartY() + r.nextDouble() * RandomLayoutSettings.getRangeY());
		}
		Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(model.getRoot(), VisualConnection.class);
		for (VisualConnection connection: connections) {
			connection.setConnectionType(ConnectionType.POLYLINE);
			connection.getGraphic().setDefaultControlPoints();
		}
	}

}