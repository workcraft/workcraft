package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.Ltl2tgbaOutput;
import org.workcraft.plugins.punf.tasks.Ltl2tgbaOutputInterpreter;
import org.workcraft.plugins.punf.tasks.PunfLtlxOutputInterpreter;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
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
        SpotChainOutput chainOutput = chainResult.getPayload();
        Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
        if (chainResult.isSuccess()) {
            PunfOutput punfOutput = (punfResult == null) ? null : punfResult.getPayload();
            return new PunfLtlxOutputInterpreter(we, punfOutput, interactive).interpret();
        }

        if (chainResult.isFailure()) {
            Result<? extends Ltl2tgbaOutput> ltl2tgbaResult = (chainOutput == null) ? null : chainOutput.getLtl2tgbaResult();
            // Process starter-sensitivity failure
            if ((punfResult == null) && (ltl2tgbaResult != null) && ltl2tgbaResult.isSuccess()) {
                Ltl2tgbaOutput ltl2tgbaOutput = (ltl2tgbaResult == null) ? null : ltl2tgbaResult.getPayload();
                new Ltl2tgbaOutputInterpreter(we, ltl2tgbaOutput, interactive).interpret();
                return null;
            }
            // Process other failures
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
        String message = "SPOT verification failed.";
        Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            message += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            SpotChainOutput chainOutput = chainResult.getPayload();
            Result<? extends Ltl2tgbaOutput> ltl2tgbaResult = (chainOutput == null) ? null : chainOutput.getLtl2tgbaResult();
            Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
            Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
            Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            if ((ltl2tgbaResult != null) && (ltl2tgbaResult.isFailure())) {
                message += "\n\nCould not derive B\u00FCchi automaton.";
                Throwable ltl2tgbaCause = ltl2tgbaResult.getCause();
                if (ltl2tgbaCause != null) {
                    message += ERROR_CAUSE_PREFIX + ltl2tgbaCause.toString();
                }
            } else  if ((exportResult != null) && (exportResult.isFailure())) {
                message += "\n\nCould not export the model as a .g file.";
                Throwable exportCause = exportResult.getCause();
                if (exportCause != null) {
                    message += ERROR_CAUSE_PREFIX + exportCause.toString();
                }
            } else if ((pcompResult != null) && (pcompResult.isFailure())) {
                message += "\n\nPcomp could not compose models.";
                Throwable pcompCause = pcompResult.getCause();
                if (pcompCause != null) {
                    message += ERROR_CAUSE_PREFIX + pcompCause.toString();
                } else {
                    PcompOutput pcompOutput = pcompResult.getPayload();
                    if (pcompOutput != null) {
                        String pcompError = pcompOutput.getErrorsHeadAndTail();
                        message += ERROR_CAUSE_PREFIX + pcompError;
                    }
                }
            } else if ((punfResult != null) && (punfResult.isFailure())) {
                message += "\n\nPunf could not verify LTL-X property.";
                Throwable punfCause = punfResult.getCause();
                if (punfCause != null) {
                    message += ERROR_CAUSE_PREFIX + punfCause.toString();
                }
            } else {
                message += "\n\nSPOT chain task returned failure status without further explanation.";
            }
        }
        return message;
    }

}
