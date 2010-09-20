package org.workcraft.plugins.verification.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;

public class MpsatDeadlockChecker extends PresetMpsatChecker implements Tool {

	public MpsatDeadlockChecker(Framework framework) {
		super(framework);
	}

	@Override
	protected String getPresetName() {
		return "Deadlock";
	}

	@Override
	public String getDisplayName() {
		return "Check for deadlocks (punf, MPSat)";
	}
}
