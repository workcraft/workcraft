package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.SynthesisTool;
import org.workcraft.plugins.mpsat.MpsatSynthesisResultHandler;
import org.workcraft.plugins.mpsat.MpsatSynthesisMode;
import org.workcraft.plugins.mpsat.MpsatSynthesisSettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

abstract public class MpsatSynthesis extends SynthesisTool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		final MpsatSynthesisSettings settings = new MpsatSynthesisSettings("Logic synthesis", getSynthesisMode(), 0);
		final MpsatChainTask task = new MpsatChainTask(we, settings);

		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(task, "MPSat logic synthesis", new MpsatSynthesisResultHandler(task));
	}

	abstract public MpsatSynthesisMode getSynthesisMode();

}
