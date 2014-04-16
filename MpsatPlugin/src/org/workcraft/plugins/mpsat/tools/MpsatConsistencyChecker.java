package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;

public class MpsatConsistencyChecker extends AbstractMpsatChecker implements Tool {

	public MpsatConsistencyChecker(Framework framework) {
		super(framework);
	}

	@Override
	public String getDisplayName() {
		return "Check for consistency [MPSat]";
	}

	@Override
	public MpsatSettings getSettings() {
		return new MpsatSettings(MpsatMode.STG_REACHABILITY, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
				MpsatSettings.reachConsistency);
	}

}
