/**
 *
 */
package org.workcraft.plugins.mpsat;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatChainResultHandler extends DummyProgressMonitor<MpsatChainResult> {
	private String errorMessage;
	private final MpsatChainTask task;

	public MpsatChainResultHandler(MpsatChainTask task) {
		this.task = task;
	}

	@Override
	public void finished(final Result<? extends MpsatChainResult> result, String description) {
		final WorkspaceEntry we = task.getWorkspaceEntry();
		MpsatChainResult returnValue = result.getReturnValue();
		Result<? extends Object> exportResult = returnValue.getExportResult();
		Result<? extends ExternalProcessResult> pcompResult = returnValue.getPcompResult();
		Result<? extends ExternalProcessResult> punfResult = returnValue.getPunfResult();
		Result<? extends ExternalProcessResult> mpsatResult = returnValue.getMpsatResult();
		if (result.getOutcome() == Outcome.FINISHED) {
			MpsatSettings mpsatSettings = returnValue.getMpsatSettings();
			switch (mpsatSettings.getMode()) {
			case UNDEFINED:
				String undefinedMessage = returnValue.getMessage();
				if ((undefinedMessage != null) && (mpsatSettings != null) && (mpsatSettings.getName() != null)) {
					undefinedMessage = mpsatSettings.getName();
				}
				SwingUtilities.invokeLater(new MpsatUndefinedResultHandler(undefinedMessage));
				break;
			case REACHABILITY:
			case STG_REACHABILITY:
			case NORMALCY:
				SwingUtilities.invokeLater(new MpsatReachabilityResultHandler(we, mpsatResult, mpsatSettings));
				break;
			case CSC_CONFLICT_DETECTION:
			case USC_CONFLICT_DETECTION:
				SwingUtilities.invokeLater(new MpsatEncodingConflictResultHandler(we, mpsatResult));
				break;
			case DEADLOCK:
				SwingUtilities.invokeLater(new MpsatDeadlockResultHandler(we, mpsatResult));
				break;
			case RESOLVE_ENCODING_CONFLICTS:
				SwingUtilities.invokeLater(new MpsatCscResolutionResultHandler(we, mpsatResult));
				break;
			default:
				final String unsupportedMessage = "MPSat mode '" + mpsatSettings.getMode().getArgument() + "' is not (yet) supported.";
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null,	unsupportedMessage, "Sorry..", JOptionPane.WARNING_MESSAGE);
					}
				});
				break;
			}
		} else if (result.getOutcome() != Outcome.CANCELLED) {
			errorMessage = "MPSat tool chain execution failed :-(";
			Throwable cause1 = result.getCause();

			if (cause1 != null) {
				// Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
				errorMessage += "\n\nFailure caused by: " + cause1.toString() + "\nPlease see the 'Problems' tab for more details.";
			} else {
				if (exportResult != null && exportResult.getOutcome() == Outcome.FAILED) {
					errorMessage += "\n\nFailed to export the model as a .g file.";
					Throwable cause = exportResult.getCause();
					if (cause != null) {
						errorMessage += "\n\nFailure caused by: " + cause.toString();
					} else {
						errorMessage += "\n\nThe exporter class did not offer further explanation.";
					}
				} else if (pcompResult != null && pcompResult.getOutcome() == Outcome.FAILED) {
					errorMessage += "\n\nPcomp could not compose the STGs.";
					Throwable cause = pcompResult.getCause();
					if (cause != null) {
						errorMessage += "\n\nFailure caused by: " + cause.toString();
					} else {
						errorMessage += "\n\nFailure caused by the following errors:\n" + new String(pcompResult.getReturnValue().getErrors());
					}
				} else if (punfResult != null && punfResult.getOutcome() == Outcome.FAILED) {
					errorMessage += "\n\nPunf could not build the unfolding prefix.";
					Throwable cause = punfResult.getCause();
					if (cause != null) {
						errorMessage += "\n\nFailure caused by: " + cause.toString();
					} else {
						errorMessage += "\n\nFailure caused by the following errors:\n" + new String(punfResult.getReturnValue().getErrors());
					}
				} else if (mpsatResult != null && mpsatResult.getOutcome() == Outcome.FAILED) {
					errorMessage += "\n\nMPSat failed to execute as expected.";
					Throwable cause = mpsatResult.getCause();
					if (cause != null) {
						errorMessage += "\n\nFailure caused by: " + cause.toString();
					} else {
						byte[] errors = mpsatResult.getReturnValue().getErrors();
						errorMessage += "\n\nFailure caused by the following errors:\n" + new String(errors);
					}
				} else {
					errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
				}
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, errorMessage, "Oops..", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}

}
