package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyNetSynthesis implements Tool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return ( WorkspaceUtils.canHas(we, PetriNetModel.class) || WorkspaceUtils.canHas(we, Fsm.class));
	}

	@Override
	public String getSection() {
		return "Net synthesis";
	}

	@Override
	public String getDisplayName() {
		return "Net synthesis (with -er option) [Petrify]";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final TransformationTask task = new TransformationTask(we, "Net synthesis", new String[] { "-er" });
		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(task, "Petrify net synthesis", new TransformationResultHandler(task));
	}

}
