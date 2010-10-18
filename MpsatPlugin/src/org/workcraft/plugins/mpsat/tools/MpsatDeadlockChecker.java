package org.workcraft.plugins.mpsat.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.mpsat.MpsatBuiltinPresets;
import org.workcraft.plugins.mpsat.MpsatSettings;

public class MpsatDeadlockChecker extends AbstractMpsatChecker implements Tool {

	public MpsatDeadlockChecker(Framework framework) {
		super(framework);
	}

	@Override
	public String getDisplayName() {
		return "Check for deadlocks (punf, MPSat)";
	}

	@Override
	protected MpsatSettings getSettings() {
		return MpsatBuiltinPresets.DEADLOCK.getSettings();
	}
}
