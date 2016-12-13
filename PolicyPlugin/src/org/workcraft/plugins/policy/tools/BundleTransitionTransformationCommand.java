package org.workcraft.plugins.policy.tools;

import org.workcraft.AbstractTransformationCommand;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class BundleTransitionTransformationCommand extends AbstractTransformationCommand {

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
        final WorkspaceEntry we = framework.getWorkspaceEntry(me);
        we.saveMemento();
        final VisualPolicyNet policy = WorkspaceUtils.getAs(me, VisualPolicyNet.class);
        policy.unbundleTransitions(policy.getVisualBundledTransitions());
        final PolicyToPetriConverter converter = new PolicyToPetriConverter(policy);
        final TransitionBundler bundler = new TransitionBundler(converter);
        bundler.run();
        framework.repaintCurrentEditor();
        return me;
    }

    @Override
    public void transform(Model model, Node node) {
    }

}
