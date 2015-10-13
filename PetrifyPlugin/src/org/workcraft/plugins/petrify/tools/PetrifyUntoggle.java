package org.workcraft.plugins.petrify.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyUntoggle extends ConversionTool {

	@Override
	public String getDisplayName() {
		return "Untoggle signal transitions [Petrify]";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		final TransformationTask task = new TransformationTask(we, "Signal transition untoggle", new String[] {"-untog"});
		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(task, "Petrify signal transition untoggle", new TransformationResultHandler(task));
	}
}
