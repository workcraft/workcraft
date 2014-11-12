package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyDummyContraction implements Tool {
	private Framework framework;

	public PetrifyDummyContraction(Framework framework) {
		this.framework = framework;
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public String getSection() {
		return "Global transformations";
	}

	@Override
	public String getDisplayName() {
		return "Dummy contraction [Petrify]";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final TransformationTask task = new TransformationTask(framework, we, "Dummy contraction", new String[] { "-hide", ".dummy" });
		framework.getTaskManager().queue(task, "Petrify dummy contraction", new TransformationResultHandler(task));
	}
}
