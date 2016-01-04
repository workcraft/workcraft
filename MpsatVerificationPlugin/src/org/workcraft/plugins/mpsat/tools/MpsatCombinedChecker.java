package org.workcraft.plugins.mpsat.tools;

import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.plugins.mpsat.MpsatCombinedChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainTask;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCombinedChecker extends VerificationTool {

	@Override
	public String getDisplayName() {
		return " Consistency, deadlock and output persistency (reuse unfolding) [MPSat]";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, PetriNetModel.class);
	}

	@Override
	public final void run(WorkspaceEntry we) {
		final ArrayList<MpsatSettings> settingsList = new ArrayList<>();
		settingsList.add(MpsatSettings.getConsistencySettings());
		settingsList.add(MpsatSettings.getDeadlockSettings());
		settingsList.add(MpsatSettings.getPersistencySettings());

		final MpsatCombinedChainTask mpsatTask = new MpsatCombinedChainTask(we, settingsList);

		String description = "MPSat tool chain";
		String title = we.getTitle();
		if (!title.isEmpty()) {
			description += "(" + title +")";
		}
		MpsatCombinedChainResultHandler monitor = new MpsatCombinedChainResultHandler(mpsatTask);
		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(mpsatTask, description, monitor);
	}

}
