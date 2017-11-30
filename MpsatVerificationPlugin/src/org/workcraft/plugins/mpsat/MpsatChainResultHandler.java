package org.workcraft.plugins.mpsat;

import java.util.Collection;

import javax.swing.SwingUtilities;

import org.workcraft.plugins.mpsat.PunfResultParser.Cause;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.AbstractResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatChainResultHandler extends AbstractResultHandler<MpsatChainResult> {

    private static final String TITLE = "Verification results";
    private static final String CANNOT_VERIFY_PREFIX = "The property cannot be verified";
    private static final String AFTER_THE_TRACE_SUFFIX = " after the following trace:\n";
    private static final String ASK_SIMULATE_SUFFIX = "\n\nSimulate the problematic trace?";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final MpsatChainTask task;
    private final Collection<Mutex> mutexes;

    public MpsatChainResultHandler(MpsatChainTask task) {
        this(task, null);
    }

    public MpsatChainResultHandler(MpsatChainTask task, Collection<Mutex> mutexes) {
        this.task = task;
        this.mutexes = mutexes;
    }

    public Collection<Mutex> getMutexes() {
        return mutexes;
    }

    @Override
    public void handleResult(final Result<? extends MpsatChainResult> result) {
        if (result.getOutcome() == Outcome.SUCCESS) {
            handleSuccess(result);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            if (!handlePartialFailure(result)) {
                handleFailure(result);
            }
        }
    }

    private void handleSuccess(final Result<? extends MpsatChainResult> result) {
        MpsatChainResult returnValue = result.getReturnValue();
        Result<? extends ExternalProcessResult> mpsatResult = (returnValue == null) ? null : returnValue.getMpsatResult();
        WorkspaceEntry we = task.getWorkspaceEntry();
        MpsatParameters mpsatSettings = returnValue.getMpsatSettings();
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
        case STG_REACHABILITY_CONSISTENCY:
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
            SwingUtilities.invokeLater(new MpsatCscConflictResolutionResultHandler(we, mpsatResult, mutexes));
            break;
        default:
            String modeString = mpsatSettings.getMode().getArgument();
            DialogUtils.showError("MPSat verification mode '" + modeString + "' is not (yet) supported.");
            break;
        }
    }

    private boolean handlePartialFailure(final Result<? extends MpsatChainResult> result) {
        WorkspaceEntry we = task.getWorkspaceEntry();
        MpsatChainResult returnValue = result.getReturnValue();
        Result<? extends ExternalProcessResult> punfResult = (returnValue == null) ? null : returnValue.getPunfResult();
        if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILURE)) {
            PunfResultParser prp = new PunfResultParser(punfResult.getReturnValue());
            Pair<MpsatSolution, PunfResultParser.Cause> punfOutcome = prp.getOutcome();
            if (punfOutcome != null) {
                MpsatSolution solution = punfOutcome.getFirst();
                Cause cause = punfOutcome.getSecond();
                MpsatParameters mpsatSettings = returnValue.getMpsatSettings();
                boolean isConsistencyCheck = (cause == Cause.INCONSISTENT)
                        && (mpsatSettings.getMode() == MpsatMode.STG_REACHABILITY_CONSISTENCY);

                if (isConsistencyCheck) {
                    int cost = solution.getMainTrace().size();
                    String mpsatFakeOutput = "SOLUTION 0\n" + solution + "\npath cost: " + cost + "\n";
                    Result<? extends ExternalProcessResult> mpsatFakeResult = Result.success(
                            new ExternalProcessResult(0, mpsatFakeOutput.getBytes(), null, null));

                    SwingUtilities.invokeLater(new MpsatReachabilityResultHandler(
                            we, mpsatFakeResult, MpsatParameters.getConsistencySettings()));
                } else {
                    String comment = solution.getComment();
                    String message = CANNOT_VERIFY_PREFIX;
                    switch (cause) {
                    case INCONSISTENT:
                        message += " for the inconsistent STG.\n\n";
                        message += comment + AFTER_THE_TRACE_SUFFIX;
                        message += solution + ASK_SIMULATE_SUFFIX;
                        if (DialogUtils.showConfirmError(message, TITLE, true)) {
                            MpsatUtils.playTrace(we, solution);
                        }
                        break;
                    case NOT_SAFE:
                        message += "for the unsafe net.\n\n";
                        message +=  comment + AFTER_THE_TRACE_SUFFIX;
                        message += solution + ASK_SIMULATE_SUFFIX;
                        if (DialogUtils.showConfirmError(message, TITLE, true)) {
                            MpsatUtils.playTrace(we, solution);
                        }
                        break;
                    case EMPTY_PRESET:
                        message += " for the malformd net.\n\n";
                        message += comment;
                        DialogUtils.showError(message, TITLE);
                        break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void handleFailure(final Result<? extends MpsatChainResult> result) {
        String errorMessage = "MPSat verification failed.";
        Throwable genericCause = result.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            MpsatChainResult returnValue = result.getReturnValue();
            Result<? extends Object> exportResult = (returnValue == null) ? null : returnValue.getExportResult();
            Result<? extends ExternalProcessResult> punfResult = (returnValue == null) ? null : returnValue.getPunfResult();
            Result<? extends ExternalProcessResult> mpsatResult = (returnValue == null) ? null : returnValue.getMpsatResult();
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
            } else if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nMPSat did not execute as expected.";
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
            } else {
                errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
            }
        }
        DialogUtils.showError(errorMessage);
    }

}
