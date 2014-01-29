package org.workcraft.plugins.policy.tools;


import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.tasks.CheckDeadlockTask;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckDeadlockTool implements Tool {
	private final Framework framework;

	public CheckDeadlockTool(Framework framework) {
		this.framework = framework;
	}

	public String getDisplayName() {
		return "Check policy net for deadlocks (taking bundels into account)";
	}

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PolicyNet;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final CheckDeadlockTask task = new CheckDeadlockTask(we, framework);
		String description = "MPSat tool chain";
		String title = we.getModelEntry().getModel().getTitle();
		if (!title.isEmpty()) {
			description += "(" + title +")";
		}
		framework.getTaskManager().queue(task, description, new MpsatChainResultHandler(task));
	}

}
