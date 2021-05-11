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
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return ConnectionUtils.isVisualConsumingArc(node) || ConnectionUtils.isVisualProducingArc(node);
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
        Collection<VisualNode> arcs = new HashSet<>();
        arcs.addAll(ConnectionUtils.getVisualConsumingArcs(model));
        arcs.addAll(ConnectionUtils.getVisualProducingArcs(model));
        Collection<VisualNode> selection = model.getSelection();
        arcs.retainAll(selection);
        return arcs;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        // Check that the arc was not removed because of a dual arc
        if ((node instanceof VisualConnection) && (node.getParent() != null)) {
            VisualConnection connection = (VisualConnection) node;
            VisualReadArc readArc = ConversionUtils.convertDirectedArcToReadArc(model, connection);
            model.addToSelection(readArc);
        }
    }

}
