package org.workcraft.plugins.dfs.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.tasks.CheckDataflowDeadlockTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckDataflowHazardTool implements Tool {
	private final Framework framework;

	public CheckDataflowHazardTool(Framework framework, Workspace ws) {
		this.framework = framework;
	}

	public String getDisplayName() {
		return "Check dataflow for hazards";
	}

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Dfs;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final CheckDataflowDeadlockTask task = new CheckDataflowDeadlockTask(we, framework);
		String description = "MPSat tool chain";
		String title = we.getModelEntry().getModel().getTitle();
		if (!title.isEmpty()) {
			description += "(" + title +")";
		}
		framework.getTaskManager().queue(task, description, new MpsatChainResultHandler(task));
	}

}
