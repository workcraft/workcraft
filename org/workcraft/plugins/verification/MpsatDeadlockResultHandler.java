/**
 *
 */
package org.workcraft.plugins.verification;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Trace;
import org.workcraft.plugins.verification.tasks.MpsatChainResult;
import org.workcraft.tasks.Result;

final class MpsatDeadlockResultHandler implements Runnable {
	private final Result<? extends MpsatChainResult> mpsatChainResult;

	MpsatDeadlockResultHandler(
			Result<? extends MpsatChainResult> mpsatChainResult) {
		this.mpsatChainResult = mpsatChainResult;
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue());

		List<Trace> solutions = mdp.getSolutions();

		if (!solutions.isEmpty()) {
			String message = "The system has a deadlock.\n";

			int i = 1;
			for (Trace t : mdp.getSolutions()) {
				if (mdp.getSolutions().size() > 1)
					message += "Trace " + Integer.toString(++i) + ": ";
				message += t.size()>0 ? t: "Initial marking is a deadlock state.";
				message += "\n";
			}

			JOptionPane.showMessageDialog(null, message);
		} else
			JOptionPane.showMessageDialog(null, "The system is deadlock-free.");
	}
}