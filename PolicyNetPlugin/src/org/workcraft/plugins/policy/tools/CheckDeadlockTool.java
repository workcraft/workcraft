package org.workcraft.plugins.policy.tools;


import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.tasks.CheckDeadlockTask;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckDeadlockTool extends VerificationTool {

	public String getDisplayName() {
		return " Deadlock with bundels [MPSat]";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PolicyNet;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final CheckDeadlockTask task = new CheckDeadlockTask(we);
		String description = "MPSat tool chain";
		String title = we.getTitle();
		if (!title.isEmpty()) {
			description += "(" + title +")";
		}
		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(task, description, new MpsatChainResultHandler(task));
	}

}
