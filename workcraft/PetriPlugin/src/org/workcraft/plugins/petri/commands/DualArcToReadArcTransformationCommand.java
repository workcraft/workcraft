package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.Set;

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
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public void transform(WorkspaceEntry we) {
        final VisualModel model = we.getModelEntry().getVisualModel();
        Collection<VisualNode> selectionOrNull = model.getSelection();
        if (selectionOrNull.isEmpty()) {
            selectionOrNull = null;
        }
        Set<Pair<VisualConnection, VisualConnection>> dualArcs
                = ConversionUtils.getSelectedOrAllDualArcs(model, selectionOrNull);

        if (!dualArcs.isEmpty()) {
            we.saveMemento();
            Set<VisualReadArc> readArcs = ConversionUtils.convertDualArcsToReadArcs(model, dualArcs);
            model.select(readArcs);
        }
    }

}
