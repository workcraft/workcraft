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

import java.util.Random;

import org.workcraft.Tool;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class RandomLayoutTool implements Tool {
	Random r = new Random();

	@Override
	public String getSection() {
		return "Layout";
	}

	@Override
	public String getDisplayName() {
		return "Randomize layout";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, VisualModel.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		VisualModel model = WorkspaceUtils.getAs(we, VisualModel.class);
		for (VisualTransformableNode node : Hierarchy.getDescendantsOfType(model.getRoot(), VisualTransformableNode.class)) {
			node.setX(RandomLayoutSettings.getStartX() + r.nextDouble() * RandomLayoutSettings.getRangeX());
			node.setY(RandomLayoutSettings.getStartY() + r.nextDouble() * RandomLayoutSettings.getRangeY());
		}
		for (VisualConnection connection: Hierarchy.getDescendantsOfType(model.getRoot(), VisualConnection.class)) {
			connection.setConnectionType(ConnectionType.POLYLINE);
			connection.getGraphic().setDefaultControlPoints();
		}
	}

}