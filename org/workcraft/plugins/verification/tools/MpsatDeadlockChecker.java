package org.workcraft.plugins.verification.tools;

import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;

@DisplayName("Check for deadlocks (punf, MPSat)")
public class MpsatDeadlockChecker extends PresetMpsatChecker implements Tool {
	@Override
	protected String getPresetName() {
		return "Deadlock";
	}
}
