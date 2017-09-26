package org.workcraft.plugins.stg.commands;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualImplicitPlaceArc;
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> connections = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            connections.addAll(stg.getVisualImplicitPlaceArcs());
            Collection<Node> selection = stg.getSelection();
            if (!selection.isEmpty()) {
                connections.retainAll(selection);
            }
        }
        return connections;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualStg) && (node instanceof VisualImplicitPlaceArc)) {
            VisualStg stg = (VisualStg) model;
            VisualImplicitPlaceArc implicitArc = (VisualImplicitPlaceArc) node;
            stg.makeExplicit(implicitArc);
        }
    }

}
