package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatDeadlockChecker extends AbstractMpsatChecker  {

	@Override
	public String getDisplayName() {
		return "Deadlock freeness [MPSat]";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, PetriNetModel.class);
	}

	@Override
	public int getPriority() {
		return 3;
	}

	@Override
	public Position getPosition() {
		return Position.TOP;
	}

	@Override
	public MpsatSettings getSettings() {
		return MpsatSettings.getDeadlockSettings();
	}

}
