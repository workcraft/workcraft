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

public class DummyToSignalTransitionConverterTool extends TransformationTool implements NodeTransformer {
    private HashSet<VisualSignalTransition> signalTransitions = null;

    @Override
    public String getDisplayName() {
        return "Convert selected dummies to signal transitions";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof STG;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualDummyTransition;
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
        HashSet<VisualDummyTransition> dummyTransitions = new HashSet<VisualDummyTransition>(model.getVisualDummyTransitions());
        dummyTransitions.retainAll(model.getSelection());
        if (!dummyTransitions.isEmpty()) {
            we.saveMemento();
            signalTransitions = new HashSet<VisualSignalTransition>(dummyTransitions.size());
            for (VisualDummyTransition dummyTransition: dummyTransitions) {
                transform(model, dummyTransition);
            }
            model.select(new LinkedList<Node>(signalTransitions));
            signalTransitions = null;
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualSTG) && (node instanceof VisualDummyTransition)) {
            VisualDummyTransition dummyTransition = (VisualDummyTransition) node;
            VisualSignalTransition signalTransition = StgUtils.convertDummyToSignalTransition((VisualSTG) model, dummyTransition);
            if (signalTransitions == null) {
                signalTransitions.add(signalTransition);
            }
        }
    }

}
