package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.workspace.WorkspaceEntry;

public class SignalToDummyTransitionConverterTool extends TransformationTool implements NodeTransformer {
    private HashSet<VisualDummyTransition> dummyTransitions = null;

    @Override
    public String getDisplayName() {
        return "Convert selected signal transitions to dummies";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof STG;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualSignalTransition;
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
    public void run(WorkspaceEntry we) {
        final VisualSTG model = (VisualSTG) we.getModelEntry().getVisualModel();
        HashSet<VisualSignalTransition> signalTransitions = new HashSet<>(model.getVisualSignalTransitions());
        signalTransitions.retainAll(model.getSelection());
        if (!signalTransitions.isEmpty()) {
            we.saveMemento();
            dummyTransitions = new HashSet<VisualDummyTransition>(signalTransitions.size());
            for (VisualSignalTransition signalTransition: signalTransitions) {
                transform(model, signalTransition);
            }
            model.select(new LinkedList<Node>(dummyTransitions));
            dummyTransitions = null;
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualSTG) && (node instanceof VisualSignalTransition)) {
            VisualSignalTransition signalTransition = (VisualSignalTransition) node;
            VisualDummyTransition dummyTransition = StgUtils.convertSignalToDummyTransition((VisualSTG) model, signalTransition);
            if (dummyTransitions == null) {
                dummyTransitions.add(dummyTransition);
            }
        }
    }

}
