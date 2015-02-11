package org.workcraft.plugins.dfs.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.tasks.CheckDataflowHazardTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckDataflowHazardTool implements Tool {

	public String getDisplayName() {
		return " Hazard [MPSat]";
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
		final CheckDataflowHazardTask task = new CheckDataflowHazardTask(we);
		String description = "MPSat tool chain";
		String title = we.getModelEntry().getModel().getTitle();
		if (!title.isEmpty()) {
			description += "(" + title +")";
		}
		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(task, description, new MpsatChainResultHandler(task));
	}

}
