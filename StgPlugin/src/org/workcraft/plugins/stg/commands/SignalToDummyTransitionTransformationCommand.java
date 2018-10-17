package org.workcraft.plugins.stg.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class SignalToDummyTransitionTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private HashSet<VisualDummyTransition> dummyTransitions = null;

    @Override
    public String getDisplayName() {
        return "Convert selected signal transitions to dummies";
    }

    @Override
    public String getPopupName() {
        return "Convert to dummy";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualSignalTransition;
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
        Collection<VisualNode> signalTransitions = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            signalTransitions.addAll(stg.getVisualSignalTransitions());
            signalTransitions.retainAll(stg.getSelection());
        }
        return signalTransitions;
    }

    @Override
    public void transform(VisualModel model, Collection<? extends VisualNode> nodes) {
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            dummyTransitions = new HashSet<>(nodes.size());
            for (VisualNode node: nodes) {
                transform(model, node);
            }
            stg.select(new LinkedList<>(dummyTransitions));
            dummyTransitions = null;
        }
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg) && (node instanceof VisualSignalTransition)) {
            VisualStg stg = (VisualStg) model;
            VisualSignalTransition signalTransition = (VisualSignalTransition) node;
            VisualDummyTransition dummyTransition = StgUtils.convertSignalToDummyTransition(stg, signalTransition);
            if (dummyTransitions != null) {
                dummyTransitions.add(dummyTransition);
            }
        }
    }

}
