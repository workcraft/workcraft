package org.workcraft.plugins.punf.tasks;

import org.workcraft.tasks.AbstractResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;

public class SpotChainResultHandler extends AbstractResultHandler<SpotChainOutput> {

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;

    public SpotChainResultHandler(WorkspaceEntry we) {
        this.we = we;
    }

    @Override
    public void handleResult(final Result<? extends SpotChainOutput> result) {
        if (result.getOutcome() == Outcome.SUCCESS) {
            handleSuccess(result);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            handleFailure(result);
        }
    }

    private void handleSuccess(final Result<? extends SpotChainOutput> chainResult) {
        SpotChainOutput chainOutput = chainResult.getPayload();
        Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
        PunfOutput punfOutput = (punfResult == null) ? null : punfResult.getPayload();
        SwingUtilities.invokeLater(new PunfLtlxOutputHandler(we, punfOutput));
    }

    private void handleFailure(final Result<? extends SpotChainOutput> chainResult) {
        String errorMessage = "Spot verification failed.";
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
                errorMessage += "\n\nSpot chain task returned failure status without further explanation.";
            }
        }
        DialogUtils.showError(errorMessage);
    }

}
