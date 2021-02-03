package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfOutputParser;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.traces.Solution;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collections;

public abstract class AbstractChainResultHandlingMonitor<T extends ChainOutput> extends AbstractResultHandlingMonitor<T, Boolean> {

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
    public final Boolean handle(Result<? extends T> result) {
        if (result.isSuccess()) {
            return handleSuccess(result);
        }

        if (result.isFailure()) {
            return handleFailure(result);
        }

        return null;
    }

    public abstract Boolean handleSuccess(Result<? extends T> chainResult);

    private Boolean handleFailure(Result<? extends T> chainResult) {
        T chainOutput = chainResult.getPayload();
        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();

        // Handle partial failure while building unfolding prefix (e.g. consistency and safeness violations)
        if ((punfResult != null) && (punfResult.isFailure())) {
            PunfOutput punfOutput = punfResult.getPayload();
            PunfOutputParser punfOutputParser = new PunfOutputParser(punfOutput);
            Pair<Solution, PunfOutputParser.Cause> punfOutcome = punfOutputParser.getOutcome();
            if (punfOutcome != null) {
                ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();
                PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();

                Solution solution = punfOutcome.getFirst();
                PunfOutputParser.Cause cause = punfOutcome.getSecond();

                MpsatOutput mpsatFakeOutput = new MpsatOutput(new ExternalProcessOutput(0),
                        ReachUtils.getConsistencyParameters(), punfOutput.getInputFile(), Collections.singletonList(solution));

                ReachabilityOutputInterpreter interpreter = new ReachabilityOutputInterpreter(we,
                        exportOutput, pcompOutput, mpsatFakeOutput, isInteractive());

                if ((cause == PunfOutputParser.Cause.INCONSISTENT) && isConsistencyCheckMode(chainOutput)) {
                    return interpreter.interpret();
                } else {
                    if (canProcessSolution()) {
                        solution = getProcessedSolution(interpreter, solution);
                    }
                    String message = "Cannot build unfolding prefix";
                    switch (cause) {
                    case INCONSISTENT:
                        message += " for the inconsistent STG.\n\n";
                        showSolutionMessage(solution, message, "");
                        return null;
                    case NOT_SAFE:
                        message += " for the unsafe net.\n\n";
                        showSolutionMessage(solution, message, " after trace");
                        return null;
                    case EMPTY_PRESET:
                        message += " for the malformed net.\n\n";
                        message += solution.getComment();
                        DialogUtils.showError(message);
                        return null;
                    }
                }
            }
        }

        // Handle complete failure of the toolchain
        String errorMessage = "MPSat verification failed.";
        Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.getMessage();
        } else {
            Result<? extends MpsatOutput> mpsatResult = getFailedMpsatResult(chainOutput);
            if ((exportResult != null) && (exportResult.isFailure())) {
                errorMessage += "\n\nCould not export the model as a .g file.";
                Throwable exportCause = exportResult.getCause();
                if (exportCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + exportCause.getMessage();
                }
            } else if ((pcompResult != null) && (pcompResult.isFailure())) {
                errorMessage += "\n\nPcomp could not compose models.";
                Throwable pcompCause = pcompResult.getCause();
                if (pcompCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + pcompCause.getMessage();
                } else {
                    PcompOutput pcompOutput = pcompResult.getPayload();
                    if (pcompOutput != null) {
                        String pcompError = pcompOutput.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + pcompError;
                    }
                }
            } else if ((punfResult != null) && (punfResult.isFailure())) {
                errorMessage += "\n\nPunf could not build the unfolding prefix.";
                Throwable punfCause = punfResult.getCause();
                if (punfCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + punfCause.getMessage();
                } else {
                    PunfOutput punfOutput = punfResult.getPayload();
                    if (punfOutput != null) {
                        String punfErrorMessage = punfOutput.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + punfErrorMessage;
                    }
                }
            } else if ((mpsatResult != null) && (mpsatResult.isFailure())) {
                errorMessage += "\n\nMPSat did not execute as expected.";
                Throwable mpsatCause = mpsatResult.getCause();
                if (mpsatCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + mpsatCause.getMessage();
                } else {
                    MpsatOutput mpsatOutput = mpsatResult.getPayload();
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
        return null;
    }

    private void showSolutionMessage(Solution solution, String prefix, String suffix) {
        String message = prefix + solution.getComment() + suffix + ":\n" + solution;
        if (canProcessSolution()) {
            message += "\n\nSimulate the problematic trace?";
            if (DialogUtils.showConfirmError(message)) {
                TraceUtils.playSolution(we, solution, suffix);
            }
        } else {
            message += "\n\nCheck consistency of the individual STG components.";
            DialogUtils.showError(message);
        }
    }

    private Solution getProcessedSolution(ReachabilityOutputInterpreter interpreter, Solution solution) {
        return interpreter.processSolutions(Collections.singletonList(solution)).iterator().next();
    }

    public abstract boolean isConsistencyCheckMode(T chainOutput);

    public abstract boolean canProcessSolution();

    public abstract Result<? extends MpsatOutput> getFailedMpsatResult(T chainOutput);

}
