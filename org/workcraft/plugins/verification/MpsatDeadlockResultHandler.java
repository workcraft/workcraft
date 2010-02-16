/**
 *
 */
package org.workcraft.plugins.verification;

import javax.swing.JOptionPane;

import org.workcraft.Trace;
import org.workcraft.plugins.verification.tasks.ExternalProcessResult;
import org.workcraft.plugins.verification.tasks.MpsatChainResult;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;

final class MpsatDeadlockResultHandler implements Runnable {
	private final Result<? extends MpsatChainResult> mpsatChainResult;

	MpsatDeadlockResultHandler(
			Result<? extends MpsatChainResult> mpsatChainResult) {
		this.mpsatChainResult = mpsatChainResult;
	}

	@Override
	public void run() {
		if (mpsatChainResult.getOutcome() == Outcome.FINISHED) {
			MpsatDeadlockParser mdp = new MpsatDeadlockParser(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue());

			if (mdp.hasDeadlock()) {
				String message = "The system has a deadlock.\n";

				int i = 1;
				for (Trace t : mdp.getSolutions()) {
					if (mdp.getSolutions().size() > 1)
						message += "Trace " + Integer.toString(i) + ": ";
					message += t.size()>0 ? t: "Initial marking is a deadlock state.";
					message += "\n";
				}

				JOptionPane.showMessageDialog(null, message);
			} else
				JOptionPane.showMessageDialog(null, "The system is deadlock-free.");
		}
		else if (mpsatChainResult.getOutcome() != Outcome.CANCELLED) {

			Result<Object> exportResult = mpsatChainResult.getReturnValue().getExportResult();
			if (exportResult.getOutcome() == Outcome.FAILED) {
				String errorMessage = "Failed to export the model as a .g file.";
				Throwable cause = exportResult.getCause();
				if (cause != null)
					errorMessage += "\nFailure caused by: " + cause.toString() + "\nPlease see the \"Problems\" tab for more details.";

				JOptionPane.showMessageDialog(null, errorMessage);
				return;
			}

			Result<ExternalProcessResult> punfResult = mpsatChainResult.getReturnValue().getPunfResult();

			if (punfResult.getOutcome() == Outcome.FAILED) {
				String errorMessage = "Punf could not build the unfolding prefix.";
				Throwable cause = punfResult.getCause();
				if (cause != null)
					errorMessage += "\n\nFailure caused by: " + cause.toString() + "\nPlease see the \"Problems\" tab for more details.";
				else
					errorMessage += "\n\nFailure caused by the following errors:\n" + new String(punfResult.getReturnValue().getErrors());
				JOptionPane.showMessageDialog(null, errorMessage, "Verification failed", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (mpsatChainResult.getReturnValue().getMpsatResult().getOutcome() == Outcome.FAILED) {
				JOptionPane.showMessageDialog(null, "Mpsat failed");
				return;
			}
		}
	}
}