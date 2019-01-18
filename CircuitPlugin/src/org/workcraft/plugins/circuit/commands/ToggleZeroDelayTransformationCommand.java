package org.workcraft.plugins.circuit.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class ToggleZeroDelayTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Toggle zero delay of selected buffers and inverters";
    }

    @Override
    public String getPopupName() {
        return "Toggle zero delay";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualCircuitComponent;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        boolean result = false;
        if (node instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) node;
            result = component.isBuffer() || component.isInverter();
        }
        return result;
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> components = new HashSet<>();
        Container root = model.getRoot();
        Collection<VisualFunctionComponent> inverters = Hierarchy.getDescendantsOfType(
                root, VisualFunctionComponent.class, component -> component.isInverter());
        components.addAll(inverters);
        Collection<VisualFunctionComponent> buffers = Hierarchy.getDescendantsOfType(
                root, VisualFunctionComponent.class, component -> component.isBuffer());
        components.addAll(buffers);
        components.retainAll(model.getSelection());
        return components;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualFunctionComponent)) {
            VisualFunctionComponent component = (VisualFunctionComponent) node;
            try {
                component.setIsZeroDelay(!component.getIsZeroDelay());
            } catch (RuntimeException e) {
                LogUtils.logWarning(e.getMessage());
            }
            model.addToSelection(component);
        }
    }

}
