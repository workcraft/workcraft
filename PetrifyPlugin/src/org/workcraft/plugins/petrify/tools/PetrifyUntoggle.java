package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyUntoggle implements Tool {
	private Framework framework;

	public PetrifyUntoggle(Framework framework) {
		this.framework = framework;
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public String getSection() {
		return "Transformations";
	}

	@Override
	public String getDisplayName() {
		return "Untoggle signal transitions (Petrify)";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final TransformationTask task = new TransformationTask(framework, we, "Signal transition untoggle", new String[] {"-untog"});
		framework.getTaskManager().queue(task, "Petrify signal transition untoggle", new TransformationResultHandler(task));
	}
}
