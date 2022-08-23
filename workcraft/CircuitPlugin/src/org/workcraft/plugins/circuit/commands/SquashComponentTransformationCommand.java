package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.plugins.circuit.utils.SquashUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;

import java.io.File;

public class SquashComponentTransformationCommand extends AbstractComponentTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Replace components by implementation (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Replace by implementation";
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
        File refinementCircuitFile = RefinementUtils.getRefinementCircuitFile(component.getReferencedComponent());
        if (refinementCircuitFile != null) {
            try {
                ModelEntry me = WorkUtils.loadModel(refinementCircuitFile);
                VisualCircuit refinementCircuit = WorkspaceUtils.getAs(me, VisualCircuit.class);
                SquashUtils.checkInterfaceConsistency(circuit, component, refinementCircuit);
                SquashUtils.squashComponent(circuit, component, refinementCircuit);
            } catch (DeserialisationException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
