package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualReplicaContact;
import org.workcraft.plugins.circuit.utils.ConversionUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public class ProxyContactTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Create proxy drivers for selected driven contacts";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Create proxy driver";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        if (node instanceof VisualConnection connection) {
            return !(connection.getFirst() instanceof VisualReplicaContact)
                    && connection.getSecond() instanceof VisualContact;
        }
        if (node instanceof VisualContact contact) {
            return contact.isDriven();
        }
        return (node instanceof VisualCircuitComponent);
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
        Collection<VisualNode> result = new HashSet<>();
        for (VisualNode node : model.getSelection()) {
            if (node instanceof VisualContact contact) {
                if (contact.isDriven()) {
                    result.add(node);
                }
            }
            if (node instanceof VisualConnection connection) {
                VisualNode secondNode = connection.getSecond();
                if (secondNode instanceof VisualContact) {
                    result.add(secondNode);
                }
            }
            if (node instanceof VisualCircuitComponent component) {
                result.addAll(component.getVisualInputs());
            }
        }
        return result;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualContact)) {
            VisualConnection connection = ConversionUtils.replicateDriverContact(
                    (VisualCircuit) model, (VisualContact) node);

            if ((connection != null) && (connection.getFirst() instanceof VisualReplicaContact)) {
                model.addToSelection(connection.getFirst());
            }
        }
    }

}
