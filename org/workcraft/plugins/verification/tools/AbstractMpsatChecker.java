package org.workcraft.plugins.verification.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.verification.MpsatChainResultHandler;
import org.workcraft.plugins.verification.MpsatSettings;
import org.workcraft.plugins.verification.tasks.MpsatChainTask;

public abstract class AbstractMpsatChecker {
	public final String getSection() {
		return "Verification";
	}

	protected abstract MpsatSettings getSettings();

	public final boolean isApplicableTo(Model model) {
		if (model instanceof STG)
			return true;
		else
			return false;
	}

	public final void run(Model model, Framework framework) {
		String title = model.getTitle();
		String description = "MPSat tool chain";
		if (!title.isEmpty())
			description += "(" + title +")";

		framework.getTaskManager().queue(new MpsatChainTask(model, getSettings(), framework), description, new MpsatChainResultHandler());
	}
}
