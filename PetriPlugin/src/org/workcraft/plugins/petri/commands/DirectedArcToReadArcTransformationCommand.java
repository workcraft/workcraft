package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class DirectedArcToReadArcTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Convert selected arcs to read-arcs";
    }

    @Override
    public String getPopupName() {
        return "Convert to read-arc";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return PetriNetUtils.isVisualConsumingArc(node) || PetriNetUtils.isVisualProducingArc(node);
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
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> arcs = new HashSet<>();
        arcs.addAll(PetriNetUtils.getVisualConsumingArcs(model));
        arcs.addAll(PetriNetUtils.getVisualProducingArcs(model));
        Collection<VisualNode> selection = model.getSelection();
        arcs.retainAll(selection);
        return arcs;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        // Check that the arc was not removed because of a dual arc
        if ((node instanceof VisualConnection) && (node.getParent() != null)) {
            VisualConnection connection = (VisualConnection) node;
            VisualReadArc readArc = PetriNetUtils.convertDirectedArcToReadArc(model, connection);
            model.addToSelection(readArc);
        }
    }

}
