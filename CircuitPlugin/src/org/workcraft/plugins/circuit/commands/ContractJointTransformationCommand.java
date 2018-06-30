package org.workcraft.plugins.circuit.commands;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
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
        return "Contract redundant joints (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Contract redundant joint";
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
        return Position.BOTTOM_MIDDLE;
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
                    VisualConnection newConnection = circuit.connect(fromNode, toNode);
                    newConnection.mixStyle(predConnection, succConnection);
                    ConnectionHelper.addControlPoints(newConnection, locations);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarning(e.getMessage());
                }
            }
        }
    }

}
