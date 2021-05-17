package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.utils.ConnectionUtils;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.types.Pair;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class ReadArcToDualArcTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Convert read-arcs to dual producing/consuming arcs (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Convert dual producing/consuming arc";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualReadArc;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> readArcs = new HashSet<>();
        readArcs.addAll(ConnectionUtils.getVisualReadArcs(model));
        Collection<VisualNode> selection = model.getSelection();
        if (!selection.isEmpty()) {
            readArcs.retainAll(selection);
        }
        return readArcs;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if (node instanceof VisualReadArc) {
            VisualReadArc readArc = (VisualReadArc) node;
            Pair<VisualConnection, VisualConnection> dualArc = ConversionUtils.convertReadArcTotDualArc(model, readArc);
            VisualConnection consumingArc = dualArc.getFirst();
            if (consumingArc != null) {
                model.addToSelection(consumingArc);
            }
            VisualConnection producingArc = dualArc.getSecond();
            if (producingArc != null) {
                model.addToSelection(producingArc);
            }
        }
    }

}
