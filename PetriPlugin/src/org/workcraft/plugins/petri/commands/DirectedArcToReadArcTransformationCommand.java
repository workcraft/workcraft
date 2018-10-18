package org.workcraft.plugins.petri.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class DirectedArcToReadArcTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private HashSet<VisualReadArc> readArcs = null;

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
    public void transform(VisualModel model, Collection<? extends VisualNode> nodes) {
        readArcs = new HashSet<>();
        for (VisualNode node: nodes) {
            // Check that the arc was not removed because of a dual arc
            if (node.getParent() != null) {
                transform(model, node);
            }
        }
        model.select(readArcs);
        readArcs = null;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) node;
            VisualReadArc readArc = PetriNetUtils.convertDirectedArcToReadArc(model, connection);
            if ((readArcs != null) && (readArc != null)) {
                readArcs.add(readArc);
            }
        }
    }

}
