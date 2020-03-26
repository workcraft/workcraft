package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.utils.TraceUtils;
import org.workcraft.traces.Solution;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.PunfOutputParser.Cause;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.AbstractResultHandler;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VerificationChainResultHandler extends AbstractResultHandler<VerificationChainOutput> {

    private static final String TITLE = "Verification results";
    private static final String CANNOT_VERIFY_PREFIX = "Cannot build unfolding prefix";
    private static final String AFTER_THE_TRACE_SUFFIX = " after the following trace:\n";
    private static final String ASK_SIMULATE_SUFFIX = "\n\nSimulate the problematic trace?";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final ArrayList<WorkspaceEntry> wes = new ArrayList<>();
    private final WorkspaceEntry we;
    private final Collection<Mutex> mutexes;

    public VerificationChainResultHandler(List<WorkspaceEntry> wes) {
        this(wes.get(0), null);
        this.wes.addAll(wes);
    }

    public VerificationChainResultHandler(WorkspaceEntry we) {
        this(we, null);
    }

    public VerificationChainResultHandler(WorkspaceEntry we, Collection<Mutex> mutexes) {
        this.we = we;
        this.mutexes = mutexes;
    }

    public Collection<Mutex> getMutexes() {
        return mutexes;
    }

    @Override
    public void handleResult(final Result<? extends VerificationChainOutput> result) {
        if (result.getOutcome() == Outcome.SUCCESS) {
            handleSuccess(result);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            if (!handlePartialFailure(result)) {
                handleFailure(result);
            }
        }
    }

    private void handleSuccess(final Result<? extends VerificationChainOutput> chainResult) {
        VerificationChainOutput chainOutput = chainResult.getPayload();
        Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
        ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
        Result<? extends VerificationOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        VerificationOutput mpsatOutput = (mpsatResult == null) ? null : mpsatResult.getPayload();
        VerificationParameters mpsatSettings = chainOutput.getMpsatSettings();
        switch (mpsatSettings.getMode()) {
        case UNDEFINED:
            String undefinedMessage = chainOutput.getMessage();
            if ((undefinedMessage == null) && (mpsatSettings != null) && (mpsatSettings.getName() != null)) {
                undefinedMessage = mpsatSettings.getName();
            }
            SwingUtilities.invokeLater(new UndefinedResultHandler(undefinedMessage));
            break;
        case REACHABILITY:
        case STG_REACHABILITY:
        case NORMALCY:
        case ASSERTION:
//            SwingUtilities.invokeLater(new ReachabilityOutputHandler(we, mpsatOutput, mpsatSettings));
            new ReachabilityOutputHandler(we, mpsatOutput, mpsatSettings).run();
            break;
        case REACHABILITY_REDUNDANCY:
//            SwingUtilities.invokeLater(new RedundancyOutputHandler(we, mpsatOutput, mpsatSettings));
            new RedundancyOutputHandler(we, mpsatOutput, mpsatSettings).run();
            break;
        case DEADLOCK:
//            SwingUtilities.invokeLater(new DeadlockFreenessOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings));
            new DeadlockFreenessOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings).run();
            break;
        case STG_REACHABILITY_CONSISTENCY:
//            SwingUtilities.invokeLater(new ConsistencyOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings));
            new ConsistencyOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings).run();
            break;
        case STG_REACHABILITY_OUTPUT_PERSISTENCY:
//            SwingUtilities.invokeLater(new OutputPersistencyOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings));
            new OutputPersistencyOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings).run();
            break;
        case STG_REACHABILITY_OUTPUT_DETERMINACY:
//            SwingUtilities.invokeLater(new OutputDeterminacyOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings));
            new OutputDeterminacyOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings).run();
            break;
        case STG_REACHABILITY_CONFORMATION:
//            SwingUtilities.invokeLater(new ConformationOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings));
            new ConformationOutputHandler(we, exportOutput, pcompOutput, mpsatOutput, mpsatSettings).run();
            break;
        case STG_REACHABILITY_CONFORMATION_NWAY:
//            SwingUtilities.invokeLater(new ConformationNwayOutputHandler(wes, exportOutput, pcompOutput, mpsatOutput, mpsatSettings));
            new ConformationNwayOutputHandler(wes, exportOutput, pcompOutput, mpsatOutput, mpsatSettings).run();
            break;
        case CSC_CONFLICT_DETECTION:
        case USC_CONFLICT_DETECTION:
//            SwingUtilities.invokeLater(new EncodingConflictOutputHandler(we, mpsatOutput));
            new EncodingConflictOutputHandler(we, mpsatOutput).run();
            break;
        case RESOLVE_ENCODING_CONFLICTS:
//            SwingUtilities.invokeLater(new CscConflictResolutionOutputHandler(we, mpsatOutput, mutexes));
            new CscConflictResolutionOutputHandler(we, mpsatOutput, mutexes).run();
            break;
        default:
            DialogUtils.showError(mpsatSettings.getMode() + " is not (yet) supported.");
            break;
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
                VerificationParameters mpsatSettings = chainOutput.getMpsatSettings();
                boolean isConsistencyCheck = (cause == Cause.INCONSISTENT)
                        && (mpsatSettings.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY);

                if (isConsistencyCheck) {
                    PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
                    int cost = solution.getMainTrace().size();
                    String mpsatFakeStdout = "SOLUTION 0\n" + solution + "\npath cost: " + cost + "\n";
                    VerificationOutput mpsatFakeOutput = new VerificationOutput(new ExternalProcessOutput(0, mpsatFakeStdout.getBytes(), new byte[0]));
                    Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
                    ExportOutput exportOutput = (exportResult == null) ? null : exportResult.getPayload();
                    SwingUtilities.invokeLater(new ConsistencyOutputHandler(
                            we, exportOutput, pcompOutput, mpsatFakeOutput, ReachUtils.getConsistencySettings()));
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
                        message +=  comment + AFTER_THE_TRACE_SUFFIX;
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
