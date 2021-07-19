package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.plugins.circuit.utils.SquashUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.WorkUtils;
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
        Pair<File, Circuit> refinementCircuit = RefinementUtils.getRefinementCircuit(component.getReferencedComponent());
        if (refinementCircuit != null) {
            try {
                ModelEntry me = WorkUtils.loadModel(refinementCircuit.getFirst());
                VisualModel componentModel = me.getVisualModel();
                SquashUtils.squashComponent(circuit, component, componentModel);
            } catch (DeserialisationException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
