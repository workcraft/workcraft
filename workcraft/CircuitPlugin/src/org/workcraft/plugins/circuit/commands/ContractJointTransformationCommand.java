package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class ContractJointTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Contract redundant joints (selected or all)";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Contract redundant joint";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualJoint;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
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
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> joints = new HashSet<>();
        joints.addAll(Hierarchy.getDescendantsOfType(model.getRoot(), VisualJoint.class));
        Collection<VisualNode> selection = model.getSelection();
        if (!selection.isEmpty()) {
            HashSet<VisualNode> selectedConnections = new HashSet<>(selection);
            selectedConnections.retainAll(joints);
            if (!selectedConnections.isEmpty()) {
                joints.retainAll(selection);
            }
        }
        return joints;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualJoint)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualJoint joint = (VisualJoint) node;
            Set<VisualConnection> connections = model.getConnections(node);
            VisualCircuitConnection predConnection = null;
            VisualCircuitConnection succConnection = null;
            boolean isRemovableJoint = true;
            for (VisualConnection connection: connections) {
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
                VisualNode fromNode = predConnection instanceof VisualCircuitConnection ? predConnection.getFirst() : null;
                VisualNode toNode = succConnection instanceof VisualCircuitConnection ? succConnection.getSecond() : null;
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
