package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractComponentTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualFunctionComponent);
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return isApplicableTo(node);
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void logNoNodesWarning(VisualModel model) {
        if (model.getSelection().isEmpty()) {
            LogUtils.logWarning("Circuit has no components that can be transformed");
        } else {
            LogUtils.logWarning("Current selection has no components that can be transformed");
        }
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> components = new HashSet<>();
        if (model instanceof VisualCircuit circuit) {
            components.addAll(Hierarchy.getDescendantsOfType(circuit.getRoot(),
                    VisualFunctionComponent.class, this::isApplicableTo));

            Collection<VisualNode> selection = circuit.getSelection();
            if (!selection.isEmpty()) {
                components.retainAll(selection);
            }
        }
        return components;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit circuit) && (node instanceof VisualFunctionComponent component)) {
            transformComponent(circuit, component);
        }
    }

    public abstract void transformComponent(VisualCircuit circuit, VisualFunctionComponent component);

}
