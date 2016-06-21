/*
*
*
* Copyright 2008,2009 Newcastle University
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
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class BufferInsertionTool extends TransformationTool implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Insert buffers into selected wires";
    }

    @Override
    public String getPopupName() {
        return "Insert buffer";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualCircuitConnection;
    }

    @Override
    public boolean isEnabled(WorkspaceEntry we, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualModel visualModel = we.getModelEntry().getVisualModel();
        if (visualModel != null) {
            Collection<VisualCircuitConnection> connections = Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualCircuitConnection.class);
            Collection<Node> selection = visualModel.getSelection();
            connections.retainAll(selection);
            if (!connections.isEmpty()) {
                we.saveMemento();
                for (VisualCircuitConnection connection: connections) {
                    transform(visualModel, connection);
                }
            }
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualCircuitConnection)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualCircuitConnection connection = (VisualCircuitConnection) node;
            Node fromNode = connection.getFirst();
            Node toNode = connection.getSecond();
            Container container = Hierarchy.getNearestContainer(fromNode, toNode);

            FunctionComponent mathComponent = new FunctionComponent();
            Container mathContainer = NamespaceHelper.getMathContainer(circuit, container);
            mathContainer.add(mathComponent);

            VisualFunctionComponent component = circuit.createVisualComponent(mathComponent, container, VisualFunctionComponent.class);
            Point2D pos = connection.getMiddleSegmentCenterPoint();
            component.setPosition(pos);

            VisualFunctionContact inputContact = circuit.getOrCreateContact(component, null, IOType.INPUT);
            inputContact.setPosition(new Point2D.Double(-1.5, 0.0));
            VisualFunctionContact outputContact = circuit.getOrCreateContact(component, null, IOType.OUTPUT);
            outputContact.setPosition(new Point2D.Double(1.5, 0.0));
            outputContact.setSetFunction(inputContact.getReferencedContact());

            LinkedList<Point2D> prefixControlPoints = ConnectionHelper.getPrefixControlPoints(connection, pos);
            LinkedList<Point2D> suffixControlPoints = ConnectionHelper.getSuffixControlPoints(connection, pos);
            circuit.remove(connection);
            try {
                VisualConnection inputConnection = (VisualCircuitConnection) circuit.connect(fromNode, inputContact);
                ConnectionHelper.addControlPoints(inputConnection, prefixControlPoints);
                VisualConnection outputConnection = (VisualCircuitConnection) circuit.connect(outputContact, toNode);
                ConnectionHelper.addControlPoints(outputConnection, suffixControlPoints);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarningLine(e.getMessage());
            }
        }
    }

}
