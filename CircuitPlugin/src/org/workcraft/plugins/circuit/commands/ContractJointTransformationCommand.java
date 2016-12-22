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

package org.workcraft.plugins.circuit.commands;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.NodeTransformer;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.commands.AbstractTransformationCommand;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ContractJointTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Contract joints (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Contract joint";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualJoint;
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        boolean result = false;
        if (node instanceof VisualJoint) {
            VisualModel visualModel = me.getVisualModel();
            if (visualModel != null) {
                result = (visualModel.getPreset(node).size() < 2) && (visualModel.getPostset(node).size() < 2);
            }
        }
        return result;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> joints = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            joints.addAll(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualJoint.class));
            Collection<Node> selection = visualModel.getSelection();
            if (!selection.isEmpty()) {
                HashSet<Node> selectedConnections = new HashSet<>(selection);
                selectedConnections.retainAll(joints);
                if (!selectedConnections.isEmpty()) {
                    joints.retainAll(selection);
                }
            }
        }
        return joints;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualJoint)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualJoint joint = (VisualJoint) node;
            Set<Connection> connections = model.getConnections(node);
            VisualCircuitConnection predConnection = null;
            VisualCircuitConnection succConnection = null;
            boolean isRemovableJoint = true;
            for (Connection connection: connections) {
                if (!(connection instanceof VisualCircuitConnection)) continue;
                if (connection.getFirst() == node) {
                    if (succConnection == null) {
                        succConnection = (VisualCircuitConnection) connection;
                    } else {
                        isRemovableJoint = false;
                    }
                }
                if (connection.getSecond() == node) {
                    if (predConnection == null) {
                        predConnection = (VisualCircuitConnection) connection;
                    } else {
                        isRemovableJoint = false;
                    }
                }
            }
            if (isRemovableJoint) {
                LinkedList<Point2D> locations = ConnectionHelper.getMergedControlPoints(joint, predConnection, succConnection);
                circuit.remove(joint);
                Node fromNode = predConnection instanceof VisualCircuitConnection ? predConnection.getFirst() : null;
                Node toNode = succConnection instanceof VisualCircuitConnection ? succConnection.getSecond() : null;
                try {
                    VisualConnection newConnection = (VisualCircuitConnection) circuit.connect(fromNode, toNode);
                    newConnection.mixStyle(predConnection, succConnection);
                    ConnectionHelper.addControlPoints(newConnection, locations);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarningLine(e.getMessage());
                }
            }
        }
    }

}
