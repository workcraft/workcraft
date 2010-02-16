package org.workcraft.plugins.verification.tools;


import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.plugins.verification.MpsatChainResultHandler;
import org.workcraft.plugins.verification.tasks.MpsatChainResult;
import org.workcraft.tasks.ProgressMonitor;

@DisplayName("Check for deadlocks (punf, MPSat)")
public class MpsatDeadlockChecker extends AbstractMpsatChecker implements Tool {
	@Override
	protected String getPresetName() {
		return "Deadlock";
	}

	@Override
	protected ProgressMonitor<MpsatChainResult> getProgressMonitor() {
		return new MpsatChainResultHandler();
	}
}
