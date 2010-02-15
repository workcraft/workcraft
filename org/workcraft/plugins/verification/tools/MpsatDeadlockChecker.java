package org.workcraft.plugins.verification.tools;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Tool;
import org.workcraft.Trace;
import org.workcraft.annotations.DisplayName;
import org.workcraft.plugins.verification.MpsatDeadlockParser;
import org.workcraft.plugins.verification.tasks.ExternalProcessResult;
import org.workcraft.plugins.verification.tasks.MpsatChainResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;

@DisplayName("Check for deadlocks (punf, MPSat)")
public class MpsatDeadlockChecker extends AbstractMpsatChecker implements Tool {
	@Override
	protected String getPresetName() {
		return "Deadlock";
	}

	@Override
	protected ProgressMonitor<MpsatChainResult> getProgressMonitor() {
		return new DummyProgressMonitor<MpsatChainResult>() {
			@Override
			public void finished(final Result<? extends MpsatChainResult> mpsatChainResult, String description) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (mpsatChainResult.getOutcome() == Outcome.FINISHED) {
							MpsatDeadlockParser mdp = new MpsatDeadlockParser(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue());

							if (mdp.hasDeadlock()) {
								String message = "The system has a deadlock.";

								int i = 1;
								for (Trace t : mdp.getSolutions()) {
									message += "\nSolution " + Integer.toString(i) + ":\n";
									message += t;
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
								Throwable cause = exportResult.getCause();
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
				});
			}
		};
	}
}
