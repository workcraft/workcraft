package org.workcraft.plugins.stg.commands;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MirrorTransitionTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Mirror transition sign (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Mirror transition sign";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        if (node instanceof VisualSignalTransition) {
            VisualSignalTransition signalTransition = (VisualSignalTransition) node;
            SignalTransition.Direction direction = signalTransition.getDirection();
            return (direction == SignalTransition.Direction.PLUS) || (direction == SignalTransition.Direction.MINUS);
        }
        return false;
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
        Collection<Node> signalTransitions = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            signalTransitions.addAll(stg.getVisualSignalTransitions());
            Collection<Node> selection = stg.getSelection();
            if (!selection.isEmpty()) {
                signalTransitions.retainAll(selection);
            }
        }
        return signalTransitions;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualStg) && (node instanceof VisualSignalTransition)) {
            VisualStg visualStg = (VisualStg) model;
            Stg stg = (Stg) visualStg.getMathModel();
            VisualSignalTransition visualTransition = (VisualSignalTransition) node;
            SignalTransition transition = visualTransition.getReferencedTransition();
            SignalTransition.Direction direction = visualTransition.getDirection();
            stg.setDirection(transition, direction.mirror());
        }
    }

}
