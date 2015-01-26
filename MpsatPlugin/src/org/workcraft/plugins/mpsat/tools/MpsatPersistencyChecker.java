package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatPersistencyChecker extends AbstractMpsatChecker implements Tool {

	@Override
	public String getDisplayName() {
		return "Output persistence (without dummies) [MPSat]";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public MpsatSettings getSettings() {
		return new MpsatSettings("Output persistence", MpsatMode.STG_REACHABILITY, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
				MpsatSettings.reachSemimodularity);

	}

}
