/**
 *
 */
package org.workcraft.plugins.mpsat;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatChainResultHandler extends DummyProgressMonitor<MpsatChainResult> {
    private static final String TITLE = "MPSat verification";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";
    private final MpsatChainTask task;

    public MpsatChainResultHandler(MpsatChainTask task) {
        this.task = task;
    }

    @Override
    public void finished(final Result<? extends MpsatChainResult> result, String description) {
        switch (result.getOutcome()) {
        case FINISHED:
            final WorkspaceEntry we = task.getWorkspaceEntry();
            handleSuccess(result, we);
            break;
        case FAILED:
            handleFailure(result);
            break;
        default:
            break;
        }
    }

    private void handleSuccess(final Result<? extends MpsatChainResult> result, WorkspaceEntry we) {
        MpsatChainResult returnValue = result.getReturnValue();
        Result<? extends ExternalProcessResult> mpsatResult = (returnValue == null) ? null : returnValue.getMpsatResult();
        MpsatSettings mpsatSettings = returnValue.getMpsatSettings();
        switch (mpsatSettings.getMode()) {
        case UNDEFINED:
            String undefinedMessage = returnValue.getMessage();
            if ((undefinedMessage == null) && (mpsatSettings != null) && (mpsatSettings.getName() != null)) {
                undefinedMessage = mpsatSettings.getName();
            }
            SwingUtilities.invokeLater(new MpsatUndefinedResultHandler(undefinedMessage));
            break;
        case REACHABILITY:
        case STG_REACHABILITY:
        case STG_REACHABILITY_OUTPUT_PERSISTENCY:
        case STG_REACHABILITY_CONFORMATION:
        case NORMALCY:
        case ASSERTION:
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
            MainWindow mainWindow = Framework.getInstance().getMainWindow();
            String modeString = mpsatSettings.getMode().getArgument();
            JOptionPane.showMessageDialog(mainWindow,
                    "Warning: MPSat verification mode '" + modeString + "' is not (yet) supported.",
                    TITLE, JOptionPane.ERROR_MESSAGE);
            break;
        }
    }

    private void handleFailure(final Result<? extends MpsatChainResult> result) {
        String errorMessage = "Error: MPSat verification failed.";
        Throwable genericCause = result.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            MpsatChainResult returnValue = result.getReturnValue();
            Result<? extends Object> exportResult = (returnValue == null) ? null : returnValue.getExportResult();
            Result<? extends ExternalProcessResult> punfResult = (returnValue == null) ? null : returnValue.getPunfResult();
            Result<? extends ExternalProcessResult> mpsatResult = (returnValue == null) ? null : returnValue.getMpsatResult();
            if ((exportResult != null) && (exportResult.getOutcome() == Outcome.FAILED)) {
                errorMessage += "\n\nCould not export the model as a .g file.";
                Throwable exportCause = exportResult.getCause();
                if (exportCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + exportCause.toString();
                }
            } else if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILED)) {
                errorMessage += "\n\nPunf could not build the unfolding prefix.";
                Throwable punfCause = punfResult.getCause();
                if (punfCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + punfCause.toString();
                } else {
                    ExternalProcessResult punfReturnValue = punfResult.getReturnValue();
                    if (punfReturnValue != null) {
                        errorMessage += ERROR_CAUSE_PREFIX + new String(punfReturnValue.getErrors());
                    }
                }
            } else if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILED)) {
                errorMessage += "\n\nMPSat did not execute as expected.";
                Throwable mpsatCause = mpsatResult.getCause();
                if (mpsatCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + mpsatCause.toString();
                } else {
                    ExternalProcessResult mpsatReturnValue = mpsatResult.getReturnValue();
                    if (mpsatReturnValue != null) {
                        String mpsatError = new String(mpsatReturnValue.getErrors());
                        errorMessage += ERROR_CAUSE_PREFIX + mpsatError;
                    }
                }
            } else {
                errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
            }
        }
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        JOptionPane.showMessageDialog(mainWindow, errorMessage, TITLE, JOptionPane.ERROR_MESSAGE);
    }

}
