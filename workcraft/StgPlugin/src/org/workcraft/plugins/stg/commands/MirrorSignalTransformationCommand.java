package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.stg.*;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public class MirrorSignalTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Mirror signals (selected or all)";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Mirror signal";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        if (node instanceof VisualSignalTransition signalTransition) {
            Signal.Type signalType = signalTransition.getSignalType();
            return (signalType == Signal.Type.INPUT) || (signalType == Signal.Type.OUTPUT);
        }
        return false;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> signalTransitions = new HashSet<>();
        if (model instanceof VisualStg stg) {
            signalTransitions.addAll(stg.getVisualSignalTransitions());
            Collection<VisualNode> selection = stg.getSelection();
            if (!selection.isEmpty()) {
                signalTransitions.retainAll(selection);
            }
        }
        return signalTransitions;
    }

    @Override
    public void transformNodes(VisualModel model, Collection<? extends VisualNode> nodes) {
        if (model instanceof VisualStg visualStg) {
            HashSet<String> processedSignals = new HashSet<>();
            Stg stg = visualStg.getMathModel();
            for (VisualNode node: nodes) {
                if (node instanceof VisualSignalTransition visualTransition) {
                    SignalTransition transition = visualTransition.getReferencedComponent();
                    String signalRef = stg.getSignalReference(transition);
                    if (!processedSignals.contains(signalRef)) {
                        transformNode(visualStg, visualTransition);
                        processedSignals.add(signalRef);
                    }
                }
            }
        }
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg visualStg) && (node instanceof VisualSignalTransition visualSignalTransition)) {
            Stg stg = visualStg.getMathModel();
            SignalTransition signalTransition = visualSignalTransition.getReferencedComponent();
            stg.setSignalType(signalTransition.getSignalName(), signalTransition.getSignalType().mirror());
        }
    }

}
