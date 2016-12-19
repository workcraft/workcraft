package org.workcraft.plugins.policy.commands;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.commands.AbstractTransformationCommand;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.policy.tools.PolicyToPetriConverter;
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
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualPolicyNet.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        we.saveMemento();
        final VisualPolicyNet policy = WorkspaceUtils.getAs(we, VisualPolicyNet.class);
        policy.unbundleTransitions(policy.getVisualBundledTransitions());
        final PolicyToPetriConverter converter = new PolicyToPetriConverter(policy);
        final TransitionBundler bundler = new TransitionBundler(converter);
        bundler.run();
        framework.repaintCurrentEditor();
    }

    @Override
    public void transform(Model model, Node node) {
    }

}
