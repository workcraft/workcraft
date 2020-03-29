package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.PunfOutputParser.Cause;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.traces.Solution;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VerificationChainResultHandlingMonitor extends AbstractResultHandlingMonitor<VerificationChainOutput, Boolean> {

    private static final String TITLE = "Verification results";
    private static final String CANNOT_VERIFY_PREFIX = "Cannot build unfolding prefix";
    private static final String AFTER_THE_TRACE_SUFFIX = " after the following trace:\n";
    private static final String ASK_SIMULATE_SUFFIX = "\n\nSimulate the problematic trace?";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final ArrayList<WorkspaceEntry> wes = new ArrayList<>();
    private final WorkspaceEntry we;
    private final boolean interactive;

    private Collection<Mutex> mutexes;

    public VerificationChainResultHandlingMonitor(List<WorkspaceEntry> wes) {
        this(wes.get(0), true);
        this.wes.addAll(wes);
    }

    public VerificationChainResultHandlingMonitor(WorkspaceEntry we, boolean interactive) {
        this.we = we;
        this.interactive = interactive;
    }

    public boolean isInteractive() {
        return interactive && Framework.getInstance().isInGuiMode();
    }

    public Collection<Mutex> getMutexes() {
        return mutexes;
    }

    public void setMutexes(Collection<Mutex> mutexes) {
        this.mutexes = mutexes;
    }

    @Override
    public Boolean handle(final Result<? extends VerificationChainOutput> result) {
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

    private Boolean handleSuccess(final Result<? extends VerificationChainOutput> chainResult) {
        VerificationChainOutput chainOutput = chainResult.getPayload();
        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
        Result<? extends VerificationOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        VerificationOutput mpsatOutput = (mpsatResult == null) ? null : mpsatResult.getPayload();
        VerificationParameters verificationParameters = chainOutput.getVerificationParameters();

        switch (verificationParameters.getMode()) {
        case UNDEFINED:
            String undefinedMessage = chainOutput.getMessage();
            if ((undefinedMessage == null) && (verificationParameters.getName() != null)) {
                undefinedMessage = verificationParameters.getName();
            }
            if (isInteractive()) {
                DialogUtils.showInfo(undefinedMessage, "Verification results");
            } else {
                LogUtils.logInfo(undefinedMessage);
            }
            return true;

        case REACHABILITY:
        case STG_REACHABILITY:
        case NORMALCY:
        case ASSERTION:
            return new ReachabilityOutputInterpreter(we, exportOutput, pcompOutput, mpsatOutput, isInteractive())
                    .interpret();

        case REACHABILITY_REDUNDANCY:
            return new RedundancyOutputInterpreter(we, exportOutput, pcompOutput, mpsatOutput, isInteractive())
                    .interpret();

        case DEADLOCK:
            return new DeadlockFreenessOutputInterpreter(we, exportOutput, pcompOutput, mpsatOutput, isInteractive())
                    .interpret();

        case STG_REACHABILITY_CONSISTENCY:
            return new ConsistencyOutputInterpreter(we, exportOutput, pcompOutput, mpsatOutput, isInteractive())
                    .interpret();

        case STG_REACHABILITY_OUTPUT_PERSISTENCY:
            return new OutputPersistencyOutputInterpreter(we, exportOutput, pcompOutput, mpsatOutput, isInteractive())
                    .interpret();

        case STG_REACHABILITY_OUTPUT_DETERMINACY:
            return new OutputDeterminacyOutputInterpreter(we, exportOutput, pcompOutput, mpsatOutput, isInteractive())
                    .interpret();

        case STG_REACHABILITY_CONFORMATION:
            return new ConformationOutputInterpreter(we, exportOutput, pcompOutput, mpsatOutput, isInteractive())
                    .interpret();

        case STG_REACHABILITY_CONFORMATION_NWAY:
            return new ConformationNwayOutputInterpreter(wes, exportOutput, pcompOutput, mpsatOutput, isInteractive())
                    .interpret();

        case CSC_CONFLICT_DETECTION:
        case USC_CONFLICT_DETECTION:
            return new EncodingConflictOutputHandler(we, mpsatOutput, isInteractive()).interpret();

        case RESOLVE_ENCODING_CONFLICTS:
            new CscConflictResolutionOutputHandler(we, mpsatOutput, mutexes).run();
            return null;

        default:
            DialogUtils.showError(verificationParameters.getMode() + " is not supported in verification.");
            return null;
        }
    }

    private boolean handlePartialFailure(final Result<? extends VerificationChainOutput> chainResult) {
        VerificationChainOutput chainOutput = chainResult.getPayload();
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
                Cause cause = punfOutcome.getSecond();
                VerificationParameters verificationParameters = chainOutput.getVerificationParameters();
                boolean isConsistencyCheck = (cause == Cause.INCONSISTENT)
                        && (verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY);

                if (isConsistencyCheck) {
                    PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
                    int cost = solution.getMainTrace().size();
                    String mpsatFakeStdout = "SOLUTION 0\n" + solution + "\npath cost: " + cost + "\n";
                    VerificationOutput mpsatFakeOutput = new VerificationOutput(
                            new ExternalProcessOutput(0, mpsatFakeStdout.getBytes(), new byte[0]),
                            null, null, ReachUtils.getConsistencyParameters());

                    Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
                    ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();
                    new ConsistencyOutputInterpreter(we, exportOutput, pcompOutput, mpsatFakeOutput, interactive).interpret();
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
                        DialogUtils.showError(message, TITLE);
                        break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void handleFailure(final Result<? extends VerificationChainOutput> chainResult) {
        String errorMessage = "MPSat verification failed.";
        Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            VerificationChainOutput chainOutput = chainResult.getPayload();
            Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
            Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
            Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            Result<? extends VerificationOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
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
                        String punfError = punfOutput.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + punfError;
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
                        String mpsatError = mpsatOutput.getErrorsHeadAndTail();
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
