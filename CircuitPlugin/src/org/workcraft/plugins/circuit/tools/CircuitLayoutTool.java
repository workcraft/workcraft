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

package org.workcraft.plugins.circuit.tools;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Random;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.layout.AbstractLayoutTool;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitLayoutTool extends AbstractLayoutTool {
	Random r = new Random();

	@Override
	public String getDisplayName() {
		return "Circuit";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, VisualCircuit.class);
	}

	@Override
	public void layout(VisualModel model) {
		setComponentPosition(model.getRoot());
		setPolylineConnections(model.getRoot());
	}

	private void setComponentPosition(Container container) {
		double x = -10.0;
		double y = 0.0;
		for (VisualContact contact: Hierarchy.getDescendantsOfType(container, VisualContact.class)) {
			if (contact.isPort() && contact.isInput()) {
				Point2D pos = new Point2D.Double(x, y);
				contact.setRootSpacePosition(pos);
				y += 2.0;
			}
		}
		x = 0.0;
		y = 0.0;
		for (VisualCircuitComponent component: Hierarchy.getDescendantsOfType(container, VisualCircuitComponent.class)) {
			Point2D pos = new Point2D.Double(x, y);
			component.setRootSpacePosition(pos);
			for (VisualContact contact: component.getContacts()) {
				if (contact.isInput()) {
					contact.setPosition(new Point2D.Double(-1.0, 0.0));
				} else {
					contact.setPosition(new Point2D.Double(1.0, 0.0));
				}
			}
			component.setContactsDefaultPosition();
			y += 4.0;
		}
		x = 10.0;
		y = 0.0;
		for (VisualContact contact: Hierarchy.getDescendantsOfType(container, VisualContact.class)) {
			if (contact.isPort() && contact.isOutput()) {
				Point2D pos = new Point2D.Double(x, y);
				contact.setRootSpacePosition(pos);
				y += 2.0;
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
