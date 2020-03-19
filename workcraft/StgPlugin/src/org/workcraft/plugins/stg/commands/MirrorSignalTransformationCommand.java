package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.stg.*;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class MirrorSignalTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Mirror signals (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Mirror signal";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        if (node instanceof VisualSignalTransition) {
            VisualSignalTransition signalTransition = (VisualSignalTransition) node;
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
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> signalTransitions = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            signalTransitions.addAll(stg.getVisualSignalTransitions());
            Collection<VisualNode> selection = stg.getSelection();
            if (!selection.isEmpty()) {
                signalTransitions.retainAll(selection);
            }
        }
        return signalTransitions;
    }

    @Override
    public void transform(VisualModel model, Collection<? extends VisualNode> nodes) {
        if (model instanceof VisualStg) {
            VisualStg visualStg = (VisualStg) model;
            HashSet<String> processedSignals = new HashSet<>();
            Stg stg = visualStg.getMathModel();
            for (VisualNode node: nodes) {
                if (node instanceof VisualSignalTransition) {
                    VisualSignalTransition visualTransition = (VisualSignalTransition) node;
                    SignalTransition transition = visualTransition.getReferencedComponent();
                    String signalRef = stg.getSignalReference(transition);
                    if (!processedSignals.contains(signalRef)) {
                        transform(visualStg, visualTransition);
                        processedSignals.add(signalRef);
                    }
                }
            }
        }
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg) && (node instanceof VisualSignalTransition)) {
            SignalTransition signalTransition = ((VisualSignalTransition) node).getReferencedComponent();
            Signal.Type signalType = signalTransition.getSignalType();
            signalTransition.setSignalType(signalType.mirror());
        }
    }

}
