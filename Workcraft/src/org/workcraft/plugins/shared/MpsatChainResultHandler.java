/**
 *
 */
package org.workcraft.plugins.shared;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.MpsatChainResult;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;

public class MpsatChainResultHandler extends DummyProgressMonitor<MpsatChainResult> {
	private String errorMessage;
	private final MpsatChainTask task;

	public MpsatChainResultHandler(MpsatChainTask task) {
		this.task = task;
	}

	@Override
	public void finished(final Result<? extends MpsatChainResult> mpsatChainResult, String description) {
		if (mpsatChainResult.getOutcome() == Outcome.FINISHED) {
			final MpsatMode mpsatMode = mpsatChainResult.getReturnValue().getMpsatSettings().getMode();
			switch (mpsatMode) {
			case DEADLOCK:
				SwingUtilities.invokeLater(new MpsatDeadlockResultHandler(task, mpsatChainResult));
				break;
			case RESOLVE_ENCODING_CONFLICTS:
				SwingUtilities.invokeLater(new MpsatCscResolutionResultHandler(task, mpsatChainResult));
				break;
			case COMPLEX_GATE_IMPLEMENTATION:
				SwingUtilities.invokeLater(new MpsatSynthesisResultHandler(task, mpsatChainResult));
				break;
			default:
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, "MPSat mode \"" + mpsatMode.getArgument() + "\" is not (yet) supported." , "Sorry..", JOptionPane.WARNING_MESSAGE);
					}
				});
				break;
			}
		}
		else if (mpsatChainResult.getOutcome() != Outcome.CANCELLED) {
			errorMessage = "MPSat tool chain execution failed :-(";

			Throwable cause1 = mpsatChainResult.getCause();

			if (cause1 != null) {
				// Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
				errorMessage += "\n\nFailure caused by: " + cause1.toString() + "\nPlease see the \"Problems\" tab for more details.";
			} else
			{
				Result<? extends Object> exportResult = mpsatChainResult.getReturnValue().getExportResult();
				if (exportResult.getOutcome() == Outcome.FAILED) {
					errorMessage += "\n\nFailed to export the model as a .g file.";
					Throwable cause = exportResult.getCause();
					if (cause != null)
						errorMessage += "\n\nFailure caused by: " + cause.toString() + "\nPlease see the \"Problems\" tab for more details.";
					else
						errorMessage += "\n\nThe exporter class did not offer further explanation.";
				} else {
					Result<? extends ExternalProcessResult> punfResult = mpsatChainResult.getReturnValue().getPunfResult();

					if (punfResult.getOutcome() == Outcome.FAILED) {
						errorMessage += "\n\nPunf could not build the unfolding prefix.";
						Throwable cause = punfResult.getCause();
						if (cause != null)
							errorMessage += "\n\nFailure caused by: " + cause.toString() + "\nPlease see the \"Problems\" tab for more details.";
						else
							errorMessage += "\n\nFailure caused by the following errors:\n" + new String(punfResult.getReturnValue().getErrors());
					} else {
						Result<? extends ExternalProcessResult> mpsatResult = mpsatChainResult.getReturnValue().getMpsatResult();

						if (mpsatResult.getOutcome() == Outcome.FAILED) {
							errorMessage += "\n\nMPSat failed to execute as expected.";
							Throwable cause = mpsatResult.getCause();
							if (cause != null)
								errorMessage += "\n\nFailure caused by: " + cause.toString() + "\nPlease see the \"Problems\" tab for more details.";
							else
								errorMessage += "\n\nFailure caused by the following errors:\n" + new String(mpsatResult.getReturnValue().getErrors());
						}
						else {
							errorMessage += "\n\nMPSat chain task returned failure status without further explanation. This should not have happened -_-a.";
						}
					}
				}
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, errorMessage, "Oops..", JOptionPane.ERROR_MESSAGE);				}
			});
		}
	}
}