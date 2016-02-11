/**
 *
 */
package org.workcraft.plugins.mpsat;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.plugins.mpsat.gui.Solution;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCombinedChainResultHandler extends DummyProgressMonitor<MpsatCombinedChainResult> {
    private String errorMessage;
    private final MpsatCombinedChainTask task;

    public MpsatCombinedChainResultHandler(MpsatCombinedChainTask task) {
        this.task = task;
    }

    @Override
    public void finished(final Result<? extends MpsatCombinedChainResult> result, String description) {
        final WorkspaceEntry we = task.getWorkspaceEntry();
        MpsatCombinedChainResult returnValue = result.getReturnValue();
        Result<? extends Object> exportResult = returnValue.getExportResult();
        Result<? extends ExternalProcessResult> pcompResult = returnValue.getPcompResult();
        Result<? extends ExternalProcessResult> punfResult = returnValue.getPunfResult();
        List<Result<? extends ExternalProcessResult>> mpsatResultList = returnValue.getMpsatResultList();
        if (result.getOutcome() == Outcome.FINISHED) {
            List<MpsatSettings> mpsatSettingsList = result.getReturnValue().getMpsatSettingsList();
            Result<? extends ExternalProcessResult> violationMpsatResult = null;
            MpsatSettings violationMpsatSettings = null;
            String verifiedMessageDetailes = "";
            for (int index = 0; index < mpsatResultList.size(); ++index) {
                Result<? extends ExternalProcessResult> mpsatResult = mpsatResultList.get(index);
                MpsatSettings mpsatSettings = mpsatSettingsList.get(index);
                MpsatResultParser mdp = new MpsatResultParser(mpsatResult.getReturnValue());
                List<Solution> solutions = mdp.getSolutions();
                if (!Solution.hasTraces(solutions)) {
                    verifiedMessageDetailes += "\n * " + mpsatSettings.getName();
                } else {
                    violationMpsatResult = mpsatResult;
                    violationMpsatSettings = mpsatSettings;
                }
            }
            // No solution found in any of the Mpsat tasks
            if (violationMpsatSettings == null) {
                final String title = "Verification results";
                final String verifiedMessage = "The following checks passed:" + verifiedMessageDetailes;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null,    verifiedMessage, title, JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                return;
            }
            // One of the Mpsat tasks returned a solution trace
            switch (violationMpsatSettings.getMode()) {
            case UNDEFINED:
                String undefinedMessage = returnValue.getMessage();
                if ((undefinedMessage != null) && (violationMpsatSettings != null) && (violationMpsatSettings.getName() != null)) {
                    undefinedMessage = violationMpsatSettings.getName();
                }
                SwingUtilities.invokeLater(new MpsatUndefinedResultHandler(undefinedMessage));
                break;
            case REACHABILITY:
            case STG_REACHABILITY:
            case NORMALCY:
                SwingUtilities.invokeLater(new MpsatReachabilityResultHandler(we, violationMpsatResult, violationMpsatSettings));
                break;
            case CSC_CONFLICT_DETECTION:
            case USC_CONFLICT_DETECTION:
                SwingUtilities.invokeLater(new MpsatEncodingConflictResultHandler(we, violationMpsatResult));
                break;
            case DEADLOCK:
                SwingUtilities.invokeLater(new MpsatDeadlockResultHandler(we, violationMpsatResult));
                break;
            case RESOLVE_ENCODING_CONFLICTS:
                SwingUtilities.invokeLater(new MpsatCscResolutionResultHandler(we, violationMpsatResult));
                break;
            default:
                final String unsupportedMessage = "MPSat mode '" + violationMpsatSettings.getMode().getArgument() + "' is not (yet) supported.";
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null,    unsupportedMessage, "Unsupported MPSat mode", JOptionPane.WARNING_MESSAGE);
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
                String errorMessageDetails = null;
                if (exportResult != null && exportResult.getOutcome() == Outcome.FAILED) {
                    errorMessageDetails = "Failed to export the model as a .g file.";
                    Throwable cause = exportResult.getCause();
                    if (cause != null) {
                        errorMessageDetails = "Failure caused by: " + cause.toString();
                    } else {
                        errorMessageDetails = "The exporter class did not offer further explanation.";
                    }
                } else if ((pcompResult != null) && (pcompResult.getOutcome() == Outcome.FAILED)) {
                    errorMessageDetails = "Pcomp could not compose the STGs.";
                    Throwable cause = pcompResult.getCause();
                    if (cause != null) {
                        errorMessageDetails = "Failure caused by: " + cause.toString();
                    } else {
                        errorMessageDetails = "Failure caused by the following errors:\n" + new String(pcompResult.getReturnValue().getErrors());
                    }
                } else if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILED)) {
                    errorMessage = "Punf could not build the unfolding prefix.";
                    Throwable cause = punfResult.getCause();
                    if (cause != null) {
                        errorMessageDetails = "Failure caused by: " + cause.toString();
                    } else {
                        errorMessageDetails = "Failure caused by the following errors:\n" + new String(punfResult.getReturnValue().getErrors());
                    }
                } else if (mpsatResultList != null) {
                    for (Result<? extends ExternalProcessResult> mpsatResult: mpsatResultList) {
                        if (mpsatResult.getOutcome() == Outcome.FAILED) {
                            errorMessageDetails = "MPSat failed to execute as expected.";
                            Throwable cause = mpsatResult.getCause();
                            if (cause != null) {
                                errorMessageDetails = "Failure caused by: " + cause.toString();
                            } else {
                                byte[] errors = mpsatResult.getReturnValue().getErrors();
                                errorMessageDetails = "Failure caused by the following errors:\n" + new String(errors);
                            }
                        }
                    }
                }
                if (errorMessageDetails == null) {
                    errorMessageDetails = "MPSat chain task returned failure status without further explanation.";
                }
                errorMessage += "\n\n" + errorMessageDetails;
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

}
