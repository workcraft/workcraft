package org.workcraft.plugins.policy.tools;

import org.workcraft.Framework;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;

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
        return WorkspaceUtils.isApplicable(me, VisualPolicyNet.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final Framework framework = Framework.getInstance();
        framework.saveMementoInCurrentWorkspaceEntry();
        final VisualPolicyNet policy = WorkspaceUtils.getAs(me, VisualPolicyNet.class);
        policy.unbundleTransitions(policy.getVisualBundledTransitions());
        final PetriNetGenerator generator = new PetriNetGenerator(policy);
        final TransitionBundler bundler = new TransitionBundler(generator);
        bundler.run();
        framework.repaintCurrentEditor();
        return me;
    }

    @Override
    public void transform(Model model, Node node) {
    }

}
