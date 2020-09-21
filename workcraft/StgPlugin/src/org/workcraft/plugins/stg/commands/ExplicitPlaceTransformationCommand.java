package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class ExplicitPlaceTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Make places explicit (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Make place explicit";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualImplicitPlaceArc;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> connections = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            connections.addAll(stg.getVisualImplicitPlaceArcs());
            Collection<VisualNode> selection = stg.getSelection();
            if (!selection.isEmpty()) {
                connections.retainAll(selection);
            }
        }
        return connections;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg) && (node instanceof VisualImplicitPlaceArc)) {
            VisualStg stg = (VisualStg) model;
            VisualImplicitPlaceArc implicitArc = (VisualImplicitPlaceArc) node;
            VisualStgPlace place = stg.makeExplicit(implicitArc);
            if (place != null) {
                model.addToSelection(place);
            }
        }
    }

}
