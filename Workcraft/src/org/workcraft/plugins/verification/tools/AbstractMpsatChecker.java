package org.workcraft.plugins.verification.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.shared.MpsatChainResultHandler;
import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.STGModel;

public abstract class AbstractMpsatChecker {

	private final Framework framework;

	public AbstractMpsatChecker(Framework framework){
		this.framework = framework;
	}

	public final String getSection() {
		return "Verification";
	}

	protected abstract MpsatSettings getSettings();

	public final boolean isApplicableTo(Model model) {
		if (model instanceof STGModel || model instanceof PetriNetModel)
			return true;
		else
			return false;
	}

	public final void run(Model model) {
		String title = model.getTitle();
		String description = "MPSat tool chain";
		if (!title.isEmpty())
			description += "(" + title +")";

		final MpsatChainTask mpsatTask = new MpsatChainTask(model, getSettings(), framework);

		framework.getTaskManager().queue(mpsatTask, description, new MpsatChainResultHandler(mpsatTask));
	}
}
