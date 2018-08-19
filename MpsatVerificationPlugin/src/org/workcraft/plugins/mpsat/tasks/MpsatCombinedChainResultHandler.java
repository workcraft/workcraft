package org.workcraft.plugins.mpsat.tasks;

import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.PunfOutputParser.Cause;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
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
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
        List<Result<? extends MpsatOutput>> mpsatResultList = chainOutput.getMpsatResultList();
        List<MpsatParameters> mpsatSettingsList = chainOutput.getMpsatSettingsList();
        MpsatOutput violationMpsatOutput = null;
        MpsatParameters violationMpsatSettings = null;
        String verifiedMessageDetailes = "";
        for (int index = 0; index < mpsatResultList.size(); ++index) {
            Result<? extends MpsatOutput> mpsatResult = mpsatResultList.get(index);
            MpsatOutput mpsatOutput = mpsatResult.getPayload();
            MpsatParameters mpsatSettings = mpsatSettingsList.get(index);
            MpsatOutputParser mdp = new MpsatOutputParser(mpsatOutput);
            List<MpsatSolution> solutions = mdp.getSolutions();
            if (!MpsatUtils.hasTraces(solutions)) {
                verifiedMessageDetailes += "\n * " + mpsatSettings.getName();
            } else {
                violationMpsatOutput = mpsatOutput;
                violationMpsatSettings = mpsatSettings;
            }
        }
        if (violationMpsatSettings == null) {
            // No solution found in any of the MPSat tasks
            if ((mutexes != null) && mutexes.isEmpty()) {
                // Add trivial mutex implementability result if no mutex places found
                verifiedMessageDetailes += "\n * Mutex implementability (trivially)";
            }
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
            case NORMALCY:
            case ASSERTION:
                SwingUtilities.invokeLater(new MpsatReachabilityOutputHandler(
                        we, violationMpsatOutput, violationMpsatSettings));
                break;
            case REACHABILITY_REDUNDANCY:
                SwingUtilities.invokeLater(new MpsatRedundancyOutputHandler(we, violationMpsatOutput, violationMpsatSettings));
                break;
            case DEADLOCK:
                SwingUtilities.invokeLater(new MpsatDeadlockFreenessOutputHandler(
                        we, pcompOutput, violationMpsatOutput, violationMpsatSettings));
                break;
            case STG_REACHABILITY_CONSISTENCY:
                SwingUtilities.invokeLater(new MpsatConsistencyOutputHandler(
                        we, pcompOutput, violationMpsatOutput, violationMpsatSettings));
                break;
            case STG_REACHABILITY_OUTPUT_PERSISTENCY:
                SwingUtilities.invokeLater(new MpsatOutputPersistencyOutputHandler(
                        we, pcompOutput, violationMpsatOutput, violationMpsatSettings));
                break;
            case STG_REACHABILITY_CONFORMATION:
                SwingUtilities.invokeLater(new MpsatConformationOutputHandler(
                        we, pcompOutput, violationMpsatOutput, violationMpsatSettings));
                break;
            case CSC_CONFLICT_DETECTION:
            case USC_CONFLICT_DETECTION:
                SwingUtilities.invokeLater(new MpsatEncodingConflictOutputHandler(we, violationMpsatOutput));
                break;
            case RESOLVE_ENCODING_CONFLICTS:
                SwingUtilities.invokeLater(new MpsatCscConflictResolutionOutputHandler(we, violationMpsatOutput, mutexes));
                break;
            default:
                DialogUtils.showError(violationMpsatSettings.getMode() + " is not (yet) supported in combined verification mode.");
                break;
            }
        }
    }

    private boolean handlePartialFailure(final Result<? extends MpsatCombinedChainOutput> chainResult) {
        MpsatCombinedChainOutput chainOutput = chainResult.getPayload();
        Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
        if ((pcompResult != null) && (pcompResult.getOutcome() == Outcome.FAILURE)) {
            return false;
        }
        Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
        if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILURE)) {
            PunfOutputParser prp = new PunfOutputParser(punfResult.getPayload());
            Pair<MpsatSolution, PunfOutputParser.Cause> punfOutcome = prp.getOutcome();
            if (punfOutcome != null) {
                MpsatSolution solution = punfOutcome.getFirst();
                Cause cause = punfOutcome.getSecond();
                boolean isConsistencyCheck = false;
                if (cause == Cause.INCONSISTENT) {
                    for (MpsatParameters mpsatSettings: chainOutput.getMpsatSettingsList()) {
                        if (mpsatSettings.getMode() == MpsatMode.STG_REACHABILITY_CONSISTENCY) {
                            isConsistencyCheck = true;
                            break;
                        }
                    }
                }
                WorkspaceEntry we = task.getWorkspaceEntry();
                if (isConsistencyCheck) {
                    PcompOutput pcompOutput = (pcompResult == null) ? null : pcompResult.getPayload();
                    int cost = solution.getMainTrace().size();
                    String mpsatFakeStdout = "SOLUTION 0\n" + solution + "\npath cost: " + cost + "\n";
                    MpsatOutput mpsatFakeOutput = new MpsatOutput(new ExternalProcessOutput(0,
                            mpsatFakeStdout.getBytes(), new byte[0]));

                    SwingUtilities.invokeLater(new MpsatConsistencyOutputHandler(
                            we, pcompOutput, mpsatFakeOutput, MpsatParameters.getConsistencySettings()));
                } else {
                    String comment = solution.getComment();
                    String message = CANNOT_VERIFY_PREFIX;
                    switch (cause) {
                    case INCONSISTENT:
                        message += " for the inconsistent STG.\n\n";
                        message += comment + AFTER_THE_TRACE_SUFFIX;
                        message += solution + ASK_SIMULATE_SUFFIX;
                        if (DialogUtils.showConfirmError(message, TITLE, true)) {
                            MpsatUtils.playSolution(we, solution);
                        }
                        break;
                    case NOT_SAFE:
                        message += " for the unsafe net.\n\n";
                        message +=  comment + AFTER_THE_TRACE_SUFFIX;
                        message += solution + ASK_SIMULATE_SUFFIX;
                        if (DialogUtils.showConfirmError(message, TITLE, true)) {
                            MpsatUtils.playSolution(we, solution);
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

    private void handleFailure(final Result<? extends MpsatCombinedChainOutput> chainResult) {
        String errorMessage = "MPSat verification failed.";
        Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            MpsatCombinedChainOutput chainOutput = chainResult.getPayload();
            Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
            Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
            Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            List<Result<? extends MpsatOutput>> mpsatResultList = (chainOutput == null) ? null : chainOutput.getMpsatResultList();
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
