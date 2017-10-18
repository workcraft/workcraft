package org.workcraft.plugins.mpsat;

import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.AbstractResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCombinedChainResultHandler extends AbstractResultHandler<MpsatCombinedChainResult> {
    private static final String ERROR_CAUSE_PREFIX = "\n\n";
    private final MpsatCombinedChainTask task;
    private final Collection<Mutex> mutexes;

    public MpsatCombinedChainResultHandler(MpsatCombinedChainTask task, Collection<Mutex> mutexes) {
        this.task = task;
        this.mutexes = mutexes;
    }

    @Override
    public void handleResult(final Result<? extends MpsatCombinedChainResult> result) {
        if (result.getOutcome() == Outcome.SUCCESS) {
            WorkspaceEntry we = task.getWorkspaceEntry();
            handleSuccess(result, we);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            handleFailure(result);
        }
    }

    private void handleSuccess(final Result<? extends MpsatCombinedChainResult> result, WorkspaceEntry we) {
        MpsatCombinedChainResult returnValue = result.getReturnValue();
        List<Result<? extends ExternalProcessResult>> mpsatResultList = returnValue.getMpsatResultList();
        List<MpsatParameters> mpsatSettingsList = returnValue.getMpsatSettingsList();
        Result<? extends ExternalProcessResult> violationMpsatResult = null;
        MpsatParameters violationMpsatSettings = null;
        String verifiedMessageDetailes = "";
        for (int index = 0; index < mpsatResultList.size(); ++index) {
            Result<? extends ExternalProcessResult> mpsatResult = mpsatResultList.get(index);
            MpsatParameters mpsatSettings = mpsatSettingsList.get(index);
            MpsatResultParser mdp = new MpsatResultParser(mpsatResult.getReturnValue());
            List<MpsatSolution> solutions = mdp.getSolutions();
            if (!MpsatUtils.hasTraces(solutions)) {
                verifiedMessageDetailes += "\n * " + mpsatSettings.getName();
            } else {
                violationMpsatResult = mpsatResult;
                violationMpsatSettings = mpsatSettings;
            }
        }
        if (violationMpsatSettings == null) {
            // No solution found in any of the Mpsat tasks
            DialogUtils.showInfo("The following checks passed:" + verifiedMessageDetailes);
        } else {
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
            case STG_REACHABILITY_OUTPUT_PERSISTENCY:
            case STG_REACHABILITY_CONFORMATION:
            case NORMALCY:
            case ASSERTION:
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
                SwingUtilities.invokeLater(new MpsatCscConflictResolutionResultHandler(we, violationMpsatResult, mutexes));
                break;
            default:
                String modeString = violationMpsatSettings.getMode().getArgument();
                DialogUtils.showError("MPSat verification mode '" + modeString + "' is not (yet) supported.");
                break;
            }
        }
    }

    private void handleFailure(final Result<? extends MpsatCombinedChainResult> result) {
        String errorMessage = "MPSat verification failed.";
        Throwable genericCause = result.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            MpsatCombinedChainResult returnValue = result.getReturnValue();
            Result<? extends Object> exportResult = (returnValue == null) ? null : returnValue.getExportResult();
            Result<? extends ExternalProcessResult> punfResult = (returnValue == null) ? null : returnValue.getPunfResult();
            List<Result<? extends ExternalProcessResult>> mpsatResultList = (returnValue == null) ? null : returnValue.getMpsatResultList();
            if ((exportResult != null) && (exportResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nCould not export the model as a .g file.";
                Throwable exportCause = exportResult.getCause();
                if (exportCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + exportCause.toString();
                }
            } else if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nPunf could not build the unfolding prefix.";
                Throwable punfCause = punfResult.getCause();
                if (punfCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + punfCause.toString();
                } else {
                    ExternalProcessResult punfReturnValue = punfResult.getReturnValue();
                    if (punfReturnValue != null) {
                        String punfError = punfReturnValue.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + punfError;
                    }
                }
            } else if (mpsatResultList != null) {
                errorMessage += "\n\nMPSat did not execute as expected.";
                for (Result<? extends ExternalProcessResult> mpsatResult: mpsatResultList) {
                    if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILURE)) {
                        Throwable mpsatCause = mpsatResult.getCause();
                        if (mpsatCause != null) {
                            errorMessage += ERROR_CAUSE_PREFIX + mpsatCause.toString();
                        } else {
                            ExternalProcessResult mpsatReturnValue = mpsatResult.getReturnValue();
                            if (mpsatReturnValue != null) {
                                String mpsatError = mpsatReturnValue.getErrorsHeadAndTail();
                                errorMessage += ERROR_CAUSE_PREFIX + mpsatError;
                            }
                        }
                    }
                }
            } else {
                errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
            }
        }
        DialogUtils.showError(errorMessage);
    }

}
