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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ComponentContractionTool extends TransformationTool implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Contract selected single-input component";
    }

    @Override
    public String getPopupName() {
        return "Contract component";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualCircuitComponent;
    }

    @Override
    public boolean isEnabled(WorkspaceEntry we, Node node) {
        boolean result = false;
        if (node instanceof VisualCircuitComponent) {
            VisualCircuitComponent component = (VisualCircuitComponent) node;
            result = component.getReferencedCircuitComponent().isSingleInputSingleOutput();
        }
        return result;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualModel visualModel = we.getModelEntry().getVisualModel();
        if (visualModel != null) {
            Collection<VisualCircuitComponent> components = Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualCircuitComponent.class);
            Collection<Node> selection = visualModel.getSelection();
            components.retainAll(selection);
            if (!components.isEmpty()) {
                we.saveMemento();
                for (VisualCircuitComponent component: components) {
                    transform(visualModel, component);
                }
            }
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualCircuitComponent)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualCircuitComponent component = (VisualCircuitComponent) node;
            Collection<VisualContact> inputContacts = component.getVisualInputs();
            if (inputContacts.size() < 2) {
                VisualContact inputContact = component.getFirstVisualInput();
                for (VisualContact outputContact: component.getVisualOutputs()) {
                    connectContacts(circuit, inputContact, outputContact);
                }
                circuit.remove(component);
            } else {
                LogUtils.logWarningLine("Cannot contract a component with more than 1 input.");
            }
        }
    }

    private void connectContacts(VisualCircuit circuit, VisualContact inputContact, VisualContact outputContact) {
        if ((inputContact != null) && (outputContact != null)) {
            for (Connection inputConnection: circuit.getConnections(inputContact)) {
                Node fromNode = inputConnection.getFirst();
                for (Connection outputConnection: new ArrayList<>(circuit.getConnections(outputContact))) {
                    Node toNode = outputConnection.getSecond();
                    LinkedList<Point2D> locations = ConnectionHelper.getMergedControlPoints((VisualContact) outputContact,
                            (VisualConnection) inputConnection, (VisualConnection) outputConnection);
                    circuit.remove(outputConnection);
                    try {
                        VisualConnection newConnection = (VisualCircuitConnection) circuit.connect(fromNode, toNode);
                        newConnection.mixStyle((VisualConnection) inputConnection, (VisualConnection) outputConnection);
                        ConnectionHelper.addControlPoints(newConnection, locations);
                    } catch (InvalidConnectionException e) {
                        LogUtils.logWarningLine(e.getMessage());
                    }
                }
            }
        }
    }

}
