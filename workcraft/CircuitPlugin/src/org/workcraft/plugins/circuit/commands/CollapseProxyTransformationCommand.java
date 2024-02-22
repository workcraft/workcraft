package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualReplicaContact;
import org.workcraft.plugins.circuit.utils.ConversionUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public class CollapseProxyTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Collapse proxy contacts (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Collapse proxy contact";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualReplicaContact;
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
        Collection<VisualNode> replicas = new HashSet<>(Hierarchy.getDescendantsOfType(
                model.getRoot(), VisualReplicaContact.class));

        Collection<VisualNode> selection = model.getSelection();
        if (!selection.isEmpty()) {
            replicas.retainAll(selection);
        }
        return replicas;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if (node instanceof VisualReplicaContact) {
            VisualReplicaContact replicaContact = (VisualReplicaContact) node;
            ConversionUtils.collapseReplicaContact(model, replicaContact);
        }
    }

}
