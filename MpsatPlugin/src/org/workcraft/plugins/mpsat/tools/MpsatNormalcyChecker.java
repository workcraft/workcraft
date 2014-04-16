package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;

public class MpsatNormalcyChecker extends AbstractMpsatChecker implements Tool {

	public MpsatNormalcyChecker(Framework framework) {
		super(framework);
	}

	@Override
	public String getDisplayName() {
		return "Check for normalcy [MPSat]";
	}

	@Override
	public MpsatSettings getSettings() {
		return new MpsatSettings("CSC", MpsatMode.NORMALCY, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
				null);
	}

}
