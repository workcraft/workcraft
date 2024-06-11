package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.CircuitPropertyHelper;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.workspace.ModelEntry;

public class UpdateComponentInterfaceTransformationCommand extends AbstractComponentTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Update interface of components from refinement model (selected or all)";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Update interface from refinement model";
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return (node instanceof VisualFunctionComponent)
                && RefinementUtils.hasRefinementCircuit((VisualFunctionComponent) node);
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

    @Override
    public void transformComponent(VisualCircuit circuit, VisualFunctionComponent component) {
        FileReference refinement = component.getReferencedComponent().getRefinement();
        CircuitPropertyHelper.setRefinementIfCompatible(circuit, component, refinement);
    }

}
