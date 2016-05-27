package org.workcraft.plugins.stg.tools;

import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.WorkspaceEntry;

public class MirrorTransitionTool extends TransformationTool implements NodeTransformer {

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
        return we.getModelEntry().getMathModel() instanceof Stg;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        if (node instanceof VisualSignalTransition) {
            VisualSignalTransition signalTransition = (VisualSignalTransition) node;
            Direction direction = signalTransition.getDirection();
            return (direction == Direction.PLUS) || (direction == Direction.MINUS);
        }
        return false;
    }

    @Override
    public boolean isEnabled(WorkspaceEntry we, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualStg model = (VisualStg) we.getModelEntry().getVisualModel();
        HashSet<VisualSignalTransition> signalTransitions = new HashSet<>(model.getVisualSignalTransitions());
        if (!model.getSelection().isEmpty()) {
            signalTransitions.retainAll(model.getSelection());
        }
        if (!signalTransitions.isEmpty()) {
            we.saveMemento();
            for (VisualSignalTransition signalTransition: signalTransitions) {
                transform(model, signalTransition);
            }
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if (node instanceof VisualSignalTransition) {
            VisualSignalTransition signalTransition = (VisualSignalTransition) node;
            Direction direction = signalTransition.getDirection();
            if (direction == Direction.PLUS) {
                signalTransition.setDirection(Direction.MINUS);
            } else if (direction == Direction.MINUS) {
                signalTransition.setDirection(Direction.PLUS);
            }
        }
    }

}
