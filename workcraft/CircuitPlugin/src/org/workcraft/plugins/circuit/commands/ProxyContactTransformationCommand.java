package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualReplicaContact;
import org.workcraft.plugins.circuit.utils.ConversionUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public class ProxyContactTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Create proxies for selected contacts";
    }

    @Override
    public String getPopupName() {
        return "Create proxy contact";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) node;
            return connection.getSecond() instanceof VisualContact;
        }
        return false;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> connections = new HashSet<>(Hierarchy.getDescendantsOfType(
                model.getRoot(), VisualConnection.class));

        connections.retainAll(model.getSelection());
        return connections;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualConnection)) {
            VisualConnection connection = ConversionUtils.replicateDriverContact(
                    (VisualCircuit) model, (VisualConnection) node);

            if ((connection != null) && (connection.getFirst() instanceof VisualReplicaContact)) {
                model.addToSelection(connection.getFirst());
            }
        }
    }

}
