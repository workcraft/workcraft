package org.workcraft.plugins.verification.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.plugins.verification.gui.MpsatBuiltinPresets;

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
