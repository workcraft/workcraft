package org.workcraft.plugins.mpsat.tasks;

import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.PunfOutputParser.Cause;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.AbstractResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCombinedChainResultHandler extends AbstractResultHandler<MpsatCombinedChainOutput> {

    private static final String TITLE = "Verification results";
    private static final String CANNOT_VERIFY_PREFIX = "The properties cannot be verified";
    private static final String AFTER_THE_TRACE_SUFFIX = " after the following trace:\n";
    private static final String ASK_SIMULATE_SUFFIX = "\n\nSimulate the problematic trace?";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final MpsatCombinedChainTask task;
    private final Collection<Mutex> mutexes;

    public MpsatCombinedChainResultHandler(MpsatCombinedChainTask task, Collection<Mutex> mutexes) {
        this.task = task;
        this.mutexes = mutexes;
    }

    @Override
    public void handleResult(final Result<? extends MpsatCombinedChainOutput> chainResult) {
        if (chainResult.getOutcome() == Outcome.SUCCESS) {
            handleSuccess(chainResult);
        } else if (chainResult.getOutcome() == Outcome.FAILURE) {
            if (!handlePartialFailure(chainResult)) {
                handleFailure(chainResult);
            }
        }
    }

    private void handleSuccess(final Result<? extends MpsatCombinedChainOutput> chainResult) {
        WorkspaceEntry we = task.getWorkspaceEntry();
        MpsatCombinedChainOutput chainOutput = chainResult.getPayload();
        List<Result<? extends MpsatOutput>> mpsatResultList = chainOutput.getMpsatResultList();
        List<MpsatParameters> mpsatSettingsList = chainOutput.getMpsatSettingsList();
        Result<? extends MpsatOutput> violationMpsatResult = null;
        MpsatParameters violationMpsatSettings = null;
        String verifiedMessageDetailes = "";
        for (int index = 0; index < mpsatResultList.size(); ++index) {
            Result<? extends MpsatOutput> mpsatResult = mpsatResultList.get(index);
            MpsatParameters mpsatSettings = mpsatSettingsList.get(index);
            MpsatOutoutParser mdp = new MpsatOutoutParser(mpsatResult.getPayload());
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
                String undefinedMessage = chainOutput.getMessage();
                if ((undefinedMessage != null) && (violationMpsatSettings != null) && (violationMpsatSettings.getName() != null)) {
                    undefinedMessage = violationMpsatSettings.getName();
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

    private boolean handlePartialFailure(final Result<? extends MpsatCombinedChainOutput> chainResult) {
        WorkspaceEntry we = task.getWorkspaceEntry();
        MpsatCombinedChainOutput chainOutptu = chainResult.getPayload();
        Result<? extends PunfOutput> punfResult = (chainOutptu == null) ? null : chainOutptu.getPunfResult();
        if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILURE)) {
            PunfOutputParser prp = new PunfOutputParser(punfResult.getPayload());
            Pair<MpsatSolution, PunfOutputParser.Cause> punfOutcome = prp.getOutcome();
            if (punfOutcome != null) {
                MpsatSolution solution = punfOutcome.getFirst();
                Cause cause = punfOutcome.getSecond();
                boolean isConsistencyCheck = false;
                if (cause == Cause.INCONSISTENT) {
                    for (MpsatParameters mpsatSettings: chainOutptu.getMpsatSettingsList()) {
                        if (mpsatSettings.getMode() == MpsatMode.STG_REACHABILITY_CONSISTENCY) {
                            isConsistencyCheck = true;
                            break;
                        }
                    }
                }
                if (isConsistencyCheck) {
                    int cost = solution.getMainTrace().size();
                    String mpsatFakeOutput = "SOLUTION 0\n" + solution + "\npath cost: " + cost + "\n";
                    Result<? extends MpsatOutput> mpsatFakeResult = Result.success(
                            new MpsatOutput(new ExternalProcessOutput(0, mpsatFakeOutput.getBytes(), new byte[0])));

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

    private void handleFailure(final Result<? extends MpsatCombinedChainOutput> chainResult) {
        String errorMessage = "MPSat verification failed.";
        Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            MpsatCombinedChainOutput chainOutput = chainResult.getPayload();
            Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
            Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            List<Result<? extends MpsatOutput>> mpsatResultList = (chainOutput == null) ? null : chainOutput.getMpsatResultList();
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
                    PunfOutput punfOutput = punfResult.getPayload();
                    if (punfOutput != null) {
                        String punfErrorMessage = punfOutput.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + punfErrorMessage;
                    }
                }
            } else if (mpsatResultList != null) {
                errorMessage += "\n\nMPSat did not execute as expected.";
                for (Result<? extends MpsatOutput> mpsatResult: mpsatResultList) {
                    if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILURE)) {
                        Throwable mpsatCause = mpsatResult.getCause();
                        if (mpsatCause != null) {
                            errorMessage += ERROR_CAUSE_PREFIX + mpsatCause.toString();
                        } else {
                            MpsatOutput mpsatOutput = mpsatResult.getPayload();
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
