package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.petrify.tasks.PetrifyDummyContractionResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifyDummyContractionTask;
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
		return "Dummy contraction";
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
