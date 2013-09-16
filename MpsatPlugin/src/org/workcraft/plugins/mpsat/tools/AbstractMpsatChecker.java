package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractMpsatChecker {

	private final Framework framework;

	public AbstractMpsatChecker(Framework framework){
		this.framework = framework;
	}

	public final String getSection() {
		return "Verification";
	}

	protected abstract MpsatSettings getSettings();

	public final boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, PetriNetModel.class);
	}

	public final void run(WorkspaceEntry we) {
		String description = "MPSat tool chain";
		String title = we.getModelEntry().getModel().getTitle();
		if (!title.isEmpty()) {
			description += "(" + title +")";
		}
		final MpsatChainTask mpsatTask = new MpsatChainTask(we, getSettings(), framework);
		framework.getTaskManager().queue(mpsatTask, description, new MpsatChainResultHandler(mpsatTask));
	}
}
