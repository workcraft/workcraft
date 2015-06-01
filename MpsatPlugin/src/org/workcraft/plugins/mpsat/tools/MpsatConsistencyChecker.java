package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatConsistencyChecker extends AbstractMpsatChecker implements Tool {

	@Override
	public String getDisplayName() {
		return " Consistency [MPSat]";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public MpsatSettings getSettings() {
		return new MpsatSettings("Consistency", MpsatMode.STG_REACHABILITY, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
				MpsatSettings.reachConsistency, true);
	}

}
