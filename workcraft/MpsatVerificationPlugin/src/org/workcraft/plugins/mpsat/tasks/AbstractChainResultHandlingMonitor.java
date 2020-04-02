package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.traces.Solution;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractChainResultHandlingMonitor<T extends ChainOutput, U> extends AbstractResultHandlingMonitor<T, U> {

    private static final String CANNOT_VERIFY_PREFIX = "Cannot build unfolding prefix";
    private static final String AFTER_THE_TRACE_SUFFIX = " after the following trace:\n";
    private static final String ASK_SIMULATE_SUFFIX = "\n\nSimulate the problematic trace?";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final boolean interactive;

    public AbstractChainResultHandlingMonitor(WorkspaceEntry we, boolean interactive) {
        this.we = we;
        this.interactive = interactive;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    public boolean isInteractive() {
        return interactive && Framework.getInstance().isInGuiMode();
    }

    @Override
    public final U handle(Result<? extends T> result) {
        if (result.getOutcome() == Outcome.SUCCESS) {
            return handleSuccess(result);
        }

        if (result.getOutcome() == Outcome.FAILURE) {
            if (!handlePartialFailure(result)) {
                handleFailure(result);
            }
        }
        return null;
    }

    public abstract U handleSuccess(Result<? extends T> chainResult);

    private boolean handlePartialFailure(Result<? extends T> chainResult) {
        T chainOutput = chainResult.getPayload();
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        if ((pcompResult != null) && (pcompResult.getOutcome() == Outcome.FAILURE)) {
            return false;
        }
        Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
        if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILURE)) {
            PunfOutputParser prp = new PunfOutputParser(punfResult.getPayload());
            Pair<Solution, PunfOutputParser.Cause> punfOutcome = prp.getOutcome();
            if (punfOutcome != null) {
                Solution solution = punfOutcome.getFirst();
                PunfOutputParser.Cause cause = punfOutcome.getSecond();

                if ((cause == PunfOutputParser.Cause.INCONSISTENT) && isConsistencyCheckMode(chainOutput)) {
                    Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
                    ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();

                    PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
                    int cost = solution.getMainTrace().size();
                    String mpsatFakeStdout = "SOLUTION 0\n" + solution + "\npath cost: " + cost + "\n";
                    VerificationOutput mpsatFakeOutput = new VerificationOutput(
                            new ExternalProcessOutput(0, mpsatFakeStdout.getBytes(), new byte[0]),
                            null, null, ReachUtils.getConsistencyParameters());

                    new ConsistencyOutputInterpreter(we, exportOutput, pcompOutput, mpsatFakeOutput, isInteractive()).interpret();
                } else {
                    String comment = solution.getComment();
                    String message = CANNOT_VERIFY_PREFIX;
                    switch (cause) {
                    case INCONSISTENT:
                        message += " for the inconsistent STG.\n\n";
                        message += comment + AFTER_THE_TRACE_SUFFIX;
                        message += solution + ASK_SIMULATE_SUFFIX;
                        if (DialogUtils.showConfirmError(message)) {
                            TraceUtils.playSolution(we, solution);
                        }
                        break;
                    case NOT_SAFE:
                        message += " for the unsafe net.\n\n";
                        message += comment + AFTER_THE_TRACE_SUFFIX;
                        message += solution + ASK_SIMULATE_SUFFIX;
                        if (DialogUtils.showConfirmError(message)) {
                            TraceUtils.playSolution(we, solution);
                        }
                        break;
                    case EMPTY_PRESET:
                        message += " for the malformed net.\n\n";
                        message += comment;
                        DialogUtils.showError(message);
                        break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public abstract boolean isConsistencyCheckMode(T chainOutput);

    private void handleFailure(Result<? extends T> chainResult) {
        String errorMessage = "MPSat verification failed.";
        Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            T chainOutput = chainResult.getPayload();
            Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
            Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
            Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            Result<? extends VerificationOutput> mpsatResult = getFailedMpsatResult(chainOutput);
            if ((exportResult != null) && (exportResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nCould not export the model as a .g file.";
                Throwable exportCause = exportResult.getCause();
                if (exportCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + exportCause.toString();
                }
            } else if ((pcompResult != null) && (pcompResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nPcomp could not compose models.";
                Throwable pcompCause = pcompResult.getCause();
                if (pcompCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + pcompCause.toString();
                } else {
                    PcompOutput pcompOutput = pcompResult.getPayload();
                    if (pcompOutput != null) {
                        String pcompError = pcompOutput.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + pcompError;
                    }
                }
            } else if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nPunf could not build the unfolding prefix.";
                Throwable punfCause = punfResult.getCause();
                if (punfCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + punfCause.toString();
                } else {
                    PunfOutput punfOutput = punfResult.getPayload();
                    if (punfOutput != null) {
                        String punfErrorMessage = punfOutput.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + punfErrorMessage;
                    }
                }
            } else if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nMPSat did not execute as expected.";
                Throwable mpsatCause = mpsatResult.getCause();
                if (mpsatCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + mpsatCause.toString();
                } else {
                    VerificationOutput mpsatOutput = mpsatResult.getPayload();
                    if (mpsatOutput != null) {
                        String mpsatErrorMessage = mpsatOutput.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + mpsatErrorMessage;
                    }
                }
            } else {
                errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
            }
        }
        DialogUtils.showError(errorMessage);
    }

    public abstract Result<? extends VerificationOutput> getFailedMpsatResult(T chainOutput);

}
