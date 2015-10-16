package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatDeadlockChecker extends AbstractMpsatChecker implements Tool {

	@Override
	public String getDisplayName() {
		return " Deadlock [MPSat]";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, PetriNetModel.class);
	}

	@Override
	public MpsatSettings getSettings() {
		return new MpsatSettings("Deadlock", MpsatMode.DEADLOCK, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount());
	}

}
