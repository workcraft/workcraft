package org.workcraft.plugins.petrify.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyNetSynthesisWithEr extends ConversionTool {

	@Override
	public String getDisplayName() {
		return "Net synthesis [Petrify with -er option]";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return (WorkspaceUtils.canHas(we, PetriNetModel.class) || WorkspaceUtils.canHas(we, Fsm.class));
	}

	@Override
	public void run(WorkspaceEntry we) {
		final TransformationTask task = new TransformationTask(we, "Net synthesis", new String[] { "-er" });
		final Framework framework = Framework.getInstance();
		boolean hasSignals = (WorkspaceUtils.canHas(we, STGModel.class) || WorkspaceUtils.canHas(we, Fst.class));
		TransformationResultHandler monitor = new TransformationResultHandler(we, hasSignals);
		framework.getTaskManager().queue(task, "Petrify net synthesis", monitor);
	}

}
