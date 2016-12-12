package org.workcraft.plugins.policy.tools;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class TransitionBundlerTool extends TransformationTool {

    @Override
    public String getDisplayName() {
        return "Bundle transitions";
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel() instanceof PolicyNet;
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final VisualPolicyNet visualModel = (VisualPolicyNet) me.getVisualModel();
        visualModel.unbundleTransitions(visualModel.getVisualBundledTransitions());
        final PetriNetGenerator generator = new PetriNetGenerator(visualModel);
        final TransitionBundler bundler = new TransitionBundler(generator);
        bundler.run();
        return me;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        we.saveMemento();
        run(we.getModelEntry());
        return we;
    }

    @Override
    public void transform(Model model, Node node) {
    }

}
