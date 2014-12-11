package org.workcraft.plugins.son.tools;


import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.tasks.ReachabilityTask;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ReachabilityTool implements Tool{

	private final Framework framework;

	public ReachabilityTool(Framework framework){

		this.framework = framework;

	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);
	}

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public String getDisplayName() {
		return "Check for reachability";
	}

	@Override
	public void run(WorkspaceEntry we) {
		ReachabilityTask task = new ReachabilityTask(we, framework);
		framework.getTaskManager().queue(task, "Verification");
	}

}
