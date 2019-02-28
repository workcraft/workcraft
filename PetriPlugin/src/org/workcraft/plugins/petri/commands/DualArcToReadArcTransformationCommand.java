package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.types.Pair;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class DualArcToReadArcTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Convert dual producing/consuming arcs to read-arcs (selected or all)";
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        final VisualModel model = we.getModelEntry().getVisualModel();
        HashSet<Pair<VisualConnection, VisualConnection>> dualArcs = PetriNetUtils.getSelectedOrAllDualArcs(model);
        if (!dualArcs.isEmpty()) {
            we.saveMemento();
            HashSet<VisualReadArc> readArcs = PetriNetUtils.convertDualArcsToReadArcs(model, dualArcs);
            model.select(readArcs);
        }
        return null;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        return new HashSet<>();
    }

    @Override
    public void transform(VisualModel model, Collection<? extends VisualNode> nodes) {
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
    }

}
