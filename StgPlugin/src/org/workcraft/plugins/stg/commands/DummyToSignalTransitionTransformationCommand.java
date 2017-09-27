package org.workcraft.plugins.stg.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualDummyTransition;
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
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
    public Collection<Node> collect(Model model) {
        Collection<Node> dummyTransitions = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            dummyTransitions.addAll(stg.getVisualDummyTransitions());
            dummyTransitions.retainAll(stg.getSelection());
        }
        return dummyTransitions;
    }

    @Override
    public void transform(Model model, Collection<Node> nodes) {
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            signalTransitions = new HashSet<VisualSignalTransition>(nodes.size());
            for (Node node: nodes) {
                transform(model, node);
            }
            visualModel.select(new LinkedList<Node>(signalTransitions));
            signalTransitions = null;
        }
    }

    @Override
    public void transform(Model model, Node node) {
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
