package org.workcraft.plugins.verification.tools;

import org.workcraft.Framework;
import org.workcraft.plugins.shared.MpsatChainResultHandler;
import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.STGModel;
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
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	public final void run(WorkspaceEntry we) {
		STGModel model = WorkspaceUtils.getAs(we, STGModel.class);
		String title = model.getTitle();
		String description = "MPSat tool chain";
		if (!title.isEmpty())
			description += "(" + title +")";

		final MpsatChainTask mpsatTask = new MpsatChainTask(we, getSettings(), framework);

		framework.getTaskManager().queue(mpsatTask, description, new MpsatChainResultHandler(mpsatTask));
	}
}
