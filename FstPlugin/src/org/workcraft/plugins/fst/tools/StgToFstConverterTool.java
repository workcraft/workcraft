package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.fst.task.StgToFstConversionResultHandler;
import org.workcraft.plugins.fst.task.WriteSgConversionTask;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToFstConverterTool extends ConversionTool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STG.class);
	}

	@Override
	public String getDisplayName() {
		return "Finate State Transducer [Petrify]";
	}

	@Override
	public void run(WorkspaceEntry we) {
		WriteSgConversionTask task = new WriteSgConversionTask(we, true);
		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(task, "Building state graph", new StgToFstConversionResultHandler(task));
	}

}
