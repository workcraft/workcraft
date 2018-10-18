package org.workcraft.plugins.stg.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class DummyToSignalTransitionTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private HashSet<VisualSignalTransition> signalTransitions = null;

    @Override
    public String getDisplayName() {
        return "Convert selected dummies to signal transitions";
    }

    @Override
    public String getPopupName() {
        return "Convert to signal transition";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualDummyTransition;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> dummyTransitions = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            dummyTransitions.addAll(stg.getVisualDummyTransitions());
            dummyTransitions.retainAll(stg.getSelection());
        }
        return dummyTransitions;
    }

    @Override
    public void transform(VisualModel model, Collection<? extends VisualNode> nodes) {
        signalTransitions = new HashSet<>(nodes.size());
        for (VisualNode node: nodes) {
            transform(model, node);
        }
        model.select(signalTransitions);
        signalTransitions = null;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg) && (node instanceof VisualDummyTransition)) {
            VisualStg stg = (VisualStg) model;
            VisualDummyTransition dummyTransition = (VisualDummyTransition) node;
            VisualSignalTransition signalTransition = StgUtils.convertDummyToSignalTransition(stg, dummyTransition);
            if (signalTransitions != null) {
                signalTransitions.add(signalTransition);
            }
        }
    }

}
