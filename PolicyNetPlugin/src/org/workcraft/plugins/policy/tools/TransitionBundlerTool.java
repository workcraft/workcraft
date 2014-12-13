package org.workcraft.plugins.policy.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.workspace.WorkspaceEntry;

public class TransitionBundlerTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Bundle transitions";
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PolicyNet;
	}


	@Override
	public void run(WorkspaceEntry we) {
		final Framework framework = Framework.getInstance();
		framework.getMainWindow().getCurrentEditor().getToolBox().selectDefaultTool();
		we.saveMemento();

		final VisualPolicyNet visualModel = (VisualPolicyNet)we.getModelEntry().getVisualModel();
		visualModel.unbundleTransitions(visualModel.getVisualBundledTransitions());

		final PetriNetGenerator generator = new PetriNetGenerator(visualModel);
		final TransitionBundler bundler = new TransitionBundler(generator);
		bundler.run();
	}
}
