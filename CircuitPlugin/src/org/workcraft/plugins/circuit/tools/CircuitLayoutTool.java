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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.layout.AbstractLayoutTool;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitLayoutTool extends AbstractLayoutTool {
	private static final double DX = 10;
	private static final double DY = 4;
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
		if (model instanceof VisualCircuit) {
			VisualCircuit circuit = (VisualCircuit)model;
			setComponentPosition(circuit);
			setPolylineConnections(circuit);
		}
	}

	private void setComponentPosition(VisualCircuit model) {
		LinkedList<HashSet<VisualComponent>> layers = rankComponents(model);
		double x = (1.0 - layers.size()) * DX / 2.0;
		for (HashSet<VisualComponent> layer: layers) {
			double y = (1.0 - layer.size()) * DY / 2.0;
			for (VisualComponent component: layer) {
				Point2D pos = new Point2D.Double(x, y);
				component.setPosition(pos);
				if (component instanceof VisualCircuitComponent) {
					VisualCircuitComponent circuitComponent = (VisualCircuitComponent)component;
					setContactPositions(circuitComponent);
				}
				y += DY;
			}
			x += DX;
		}
	}

	private void setContactPositions(VisualCircuitComponent circuitComponent) {
		for (VisualContact contact: circuitComponent.getContacts()) {
			if (contact.isInput()) {
				contact.setPosition(new Point2D.Double(-1.0, 0.0));
			} else {
				contact.setPosition(new Point2D.Double(1.0, 0.0));
			}
		}
		circuitComponent.setContactsDefaultPosition();
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
		for (VisualContact contact: Hierarchy.getDescendantsOfType(model.getRoot(), VisualContact.class)) {
			if (contact.isPort() && contact.isOutput()) {
				outputPorts.add(contact);
			}
		}
		result.add(outputPorts);
		return result;
	}

	private HashSet<VisualComponent> getNextLayer(final VisualCircuit model, HashSet<VisualComponent>layer) {
		HashSet<VisualComponent> result = new HashSet<>();
		for (VisualComponent component: layer) {
			result.addAll(CircuitUtils.getComponentPostset(model, component));
		}
		return result;
	}

	private void setPolylineConnections(VisualCircuit model) {
		for (VisualConnection connection: Hierarchy.getDescendantsOfType(model.getRoot(), VisualConnection.class)) {
			connection.setConnectionType(ConnectionType.POLYLINE);
			connection.getGraphic().setDefaultControlPoints();
		}
	}

}
