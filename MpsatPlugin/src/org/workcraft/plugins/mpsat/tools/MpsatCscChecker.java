package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;

public class MpsatCscChecker extends AbstractMpsatChecker implements Tool {

	public MpsatCscChecker(Framework framework) {
		super(framework);
	}

	@Override
	public String getDisplayName() {
		return "Check for CSC [MPSat]";
	}

	@Override
	public MpsatSettings getSettings() {
		return new MpsatSettings("CSC", MpsatMode.CSC_CONFLICT_DETECTION, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
				null);
	}

}
