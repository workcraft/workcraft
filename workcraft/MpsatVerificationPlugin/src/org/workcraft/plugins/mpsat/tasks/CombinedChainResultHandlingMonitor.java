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

import java.util.Collection;
import java.util.List;

public class CombinedChainResultHandlingMonitor extends AbstractResultHandlingMonitor<CombinedChainOutput, Boolean> {

    private static final String TITLE = "Verification results";
    private static final String CANNOT_VERIFY_PREFIX = "The properties cannot be verified";
    private static final String AFTER_THE_TRACE_SUFFIX = " after the following trace:\n";
    private static final String ASK_SIMULATE_SUFFIX = "\n\nSimulate the problematic trace?";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final boolean interactive;
    private Collection<Mutex> mutexes;

    public CombinedChainResultHandlingMonitor(WorkspaceEntry we, boolean interactive) {
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
    public Boolean handle(final Result<? extends CombinedChainOutput> chainResult) {
        if (chainResult.getOutcome() == Outcome.SUCCESS) {
            return handleSuccess(chainResult);
        }

        if (chainResult.getOutcome() == Outcome.FAILURE) {
            if (!handlePartialFailure(chainResult)) {
                handleFailure(chainResult);
            }
        }
        return null;
    }

    private Boolean handleSuccess(final Result<? extends CombinedChainOutput> chainResult) {
        CombinedChainOutput chainOutput = chainResult.getPayload();
        List<Result<? extends VerificationOutput>> mpsatResultList = chainOutput.getMpsatResultList();
        List<VerificationParameters> verificationParametersList = chainOutput.getVerificationParametersList();

        VerificationOutput violationMpsatOutput = null;
        VerificationParameters violationVerificationParameters = null;
        String verifiedMessageDetailes = "";
        for (int index = 0; index < mpsatResultList.size(); ++index) {
            VerificationParameters verificationParameters = verificationParametersList.get(index);
            Result<? extends VerificationOutput> mpsatResult = mpsatResultList.get(index);
            boolean hasSolutions = false;
            if (mpsatResult != null) {
                String mpsatStdout = mpsatResult.getPayload().getStdoutString();
                VerificationOutputParser mdp = new VerificationOutputParser(mpsatStdout);
                List<Solution> solutions = mdp.getSolutions();
                hasSolutions = TraceUtils.hasTraces(solutions);
            }
            if (!hasSolutions) {
                verifiedMessageDetailes += "\n * " + verificationParameters.getName();
            } else {
                violationMpsatOutput = mpsatResult.getPayload();
                violationVerificationParameters = verificationParameters;
            }
        }

        if (violationVerificationParameters == null) {
            // No solution found in any of the MPSat tasks
            if ((mutexes != null) && mutexes.isEmpty()) {
                // Add trivial mutex implementability result if no mutex places found
                verifiedMessageDetailes += "\n * Mutex implementability (vacuously)";
            }
            DialogUtils.showInfo(verifiedMessageDetailes.isEmpty() ? chainOutput.getMessage()
                    : "The following checks passed:" + verifiedMessageDetailes);

            return true;
        }

        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
        // One of the Mpsat tasks returned a solution trace
        switch (violationVerificationParameters.getMode()) {
        case UNDEFINED:
            String undefinedMessage = chainOutput.getMessage();
            if ((undefinedMessage != null) && (violationVerificationParameters.getName() != null)) {
                undefinedMessage = violationVerificationParameters.getName();
            }
            if (isInteractive()) {
                DialogUtils.showInfo(undefinedMessage, "Verification results");
            } else {
                LogUtils.logInfo(undefinedMessage);
            }
            return null;

        case REACHABILITY:
        case STG_REACHABILITY:
        case NORMALCY:
        case ASSERTION:
            return new ReachabilityOutputInterpreter(we, exportOutput, pcompOutput, violationMpsatOutput, interactive)
                    .interpret();

        case REACHABILITY_REDUNDANCY:
            return new RedundancyOutputInterpreter(we, exportOutput, pcompOutput, violationMpsatOutput, interactive)
                    .interpret();

        case DEADLOCK:
            return new DeadlockFreenessOutputInterpreter(we, exportOutput, pcompOutput, violationMpsatOutput, interactive)
                    .interpret();

        case STG_REACHABILITY_CONSISTENCY:
            return new ConsistencyOutputInterpreter(we, exportOutput, pcompOutput, violationMpsatOutput, interactive)
                    .interpret();

        case STG_REACHABILITY_OUTPUT_PERSISTENCY:
            return new OutputPersistencyOutputInterpreter(we, exportOutput, pcompOutput, violationMpsatOutput, interactive)
                    .interpret();

        case STG_REACHABILITY_OUTPUT_DETERMINACY:
            return new OutputDeterminacyOutputInterpreter(we, exportOutput, pcompOutput, violationMpsatOutput, interactive)
                    .interpret();

        case STG_REACHABILITY_CONFORMATION:
            return new ConformationOutputInterpreter(we, exportOutput, pcompOutput, violationMpsatOutput, interactive)
                    .interpret();

        case CSC_CONFLICT_DETECTION:
        case USC_CONFLICT_DETECTION:
            return new EncodingConflictOutputHandler(we, violationMpsatOutput, interactive).interpret();

        case RESOLVE_ENCODING_CONFLICTS:
            new CscConflictResolutionOutputHandler(we, violationMpsatOutput, mutexes).run();
            return null;

        default:
            DialogUtils.showError(violationVerificationParameters.getMode() + " is not supported in combined verification.");

            return null;
        }
    }

    private boolean handlePartialFailure(final Result<? extends CombinedChainOutput> chainResult) {
        CombinedChainOutput chainOutput = chainResult.getPayload();
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
                boolean isConsistencyCheck = false;
                if (cause == Cause.INCONSISTENT) {
                    for (VerificationParameters verificationParameters: chainOutput.getVerificationParametersList()) {
                        if (verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY) {
                            isConsistencyCheck = true;
                            break;
                        }
                    }
                }
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

    private void handleFailure(final Result<? extends CombinedChainOutput> chainResult) {
        String errorMessage = "MPSat verification failed.";
        Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            CombinedChainOutput chainOutput = chainResult.getPayload();
            Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
            Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
            Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            List<Result<? extends VerificationOutput>> mpsatResultList = (chainOutput == null) ? null : chainOutput.getMpsatResultList();
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
            } else if (mpsatResultList != null) {
                errorMessage += "\n\nMPSat did not execute as expected.";
                for (Result<? extends VerificationOutput> mpsatResult: mpsatResultList) {
                    if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILURE)) {
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
                    }
                }
            } else {
                errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
            }
        }
        DialogUtils.showError(errorMessage);
    }

}
