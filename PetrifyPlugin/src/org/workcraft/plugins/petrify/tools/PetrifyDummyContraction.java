package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyDummyContraction implements Tool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public String getDisplayName() {
		return "Dummy contraction [Petrify]";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final TransformationTask task = new TransformationTask(we, "Dummy contraction", new String[] { "-hide", ".dummy" });
		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(task, "Petrify dummy contraction", new TransformationResultHandler(task));
	}
}
