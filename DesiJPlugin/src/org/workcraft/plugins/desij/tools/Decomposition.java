package org.workcraft.plugins.desij.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.desij.DecompositionResultHandler;
import org.workcraft.plugins.desij.tasks.DesiJTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class Decomposition implements Tool {

	private final Framework framework;

	public Decomposition(Framework framework) {
		this.framework = framework;
	}

	@Override
	public String getSection() {
		return "Decomposition";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		// call desiJ asynchronous (w/o blocking the GUI)
		DesiJTask task = new DesiJTask(WorkspaceUtils.getAs(we, STGModel.class), we, framework, new String[0]);
		framework.getTaskManager().queue(task,
				"Execution of DesiJ", new DecompositionResultHandler(framework, false, task));

	}

	@Override
	public String getDisplayName() {
		return "Standard decomposition (DesiJ)";
	}
}
