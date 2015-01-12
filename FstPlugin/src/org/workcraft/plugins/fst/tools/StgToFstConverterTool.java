package org.workcraft.plugins.fst.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.fst.task.ConversionResultHandler;
import org.workcraft.plugins.fst.task.ConversionTask;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToFstConverterTool implements Tool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, PetriNetModel.class);
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public String getDisplayName() {
		return "Finate State Transducer [Petrify]";
	}

	@Override
	public void run(WorkspaceEntry we) {
		ConversionTask task = new ConversionTask(we);
		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(task, "Building state graph", new ConversionResultHandler(task));
	}

}
