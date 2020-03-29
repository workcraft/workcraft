package org.workcraft.plugins.punf.tasks;

import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class SpotChainResultHandlingMonitor extends AbstractResultHandlingMonitor<SpotChainOutput, Boolean> {

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final boolean interactive;

    public SpotChainResultHandlingMonitor(WorkspaceEntry we,  boolean interactive) {
        this.we = we;
        this.interactive = interactive;
    }

    @Override
    public Boolean handle(final Result<? extends SpotChainOutput> chainResult) {
        if (chainResult.getOutcome() == Outcome.SUCCESS) {
            SpotChainOutput chainOutput = chainResult.getPayload();
            Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            PunfOutput punfOutput = (punfResult == null) ? null : punfResult.getPayload();
            return new PunfLtlxOutputInterpreter(we, punfOutput, interactive).interpret();
        }

        if (chainResult.getOutcome() == Outcome.FAILURE) {
            String message = buildFailureMessage(chainResult);
            if (message != null) {
                if (interactive) {
                    DialogUtils.showError(message);
                } else {
                    LogUtils.logError(message);
                }
            }
        }

        return null;
    }

    private String buildFailureMessage(final Result<? extends SpotChainOutput> chainResult) {
        String errorMessage = "SPOT verification failed.";
        Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            SpotChainOutput chainOutput = chainResult.getPayload();
            Result<? extends Ltl2tgbaOutput> ltl2tgbaResult = (chainOutput == null) ? null : chainOutput.getLtl2tgbaResult();
            Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            if ((ltl2tgbaResult != null) && (ltl2tgbaResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nCould not derive B\u00FCchi automaton.";
                Throwable exportCause = ltl2tgbaResult.getCause();
                if (exportCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + exportCause.toString();
                }
            } else if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nPunf could not verify LTL-X property.";
                Throwable punfCause = punfResult.getCause();
                if (punfCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + punfCause.toString();
                }
            } else {
                errorMessage += "\n\nSPOT chain task returned failure status without further explanation.";
            }
        }
        return errorMessage;
    }

}
