package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.petrify.tasks.PetrifyDummyContractionResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifyDummyContractionTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class DummyContraction implements Tool {
	private Framework framework;

	public DummyContraction(Framework framework) {
		this.framework = framework;
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public String getSection() {
		return "Misc.";
	}

	@Override
	public String getDisplayName() {
		return "Contract dummies (Petrify)";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final PetrifyDummyContractionTask task = new PetrifyDummyContractionTask(framework, we);
		framework.getTaskManager().queue(task, "Petrify dummy contraction", new PetrifyDummyContractionResultHandler(task));
	}
}
