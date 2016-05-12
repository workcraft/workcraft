package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.workspace.WorkspaceEntry;

public class DummyToSignalTransitionConverterTool extends TransformationTool implements NodeTransformer {
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
        return we.getModelEntry().getMathModel() instanceof Stg;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualDummyTransition;
    }

    @Override
    public boolean isEnabled(WorkspaceEntry we, Node node) {
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
    public void run(WorkspaceEntry we) {
        final VisualStg model = (VisualStg) we.getModelEntry().getVisualModel();
        HashSet<VisualDummyTransition> dummyTransitions = new HashSet<>(model.getVisualDummyTransitions());
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
        if ((model instanceof VisualStg) && (node instanceof VisualDummyTransition)) {
            VisualDummyTransition dummyTransition = (VisualDummyTransition) node;
            VisualSignalTransition signalTransition = StgUtils.convertDummyToSignalTransition((VisualStg) model, dummyTransition);
            if (signalTransitions == null) {
                signalTransitions.add(signalTransition);
            }
        }
    }

}
