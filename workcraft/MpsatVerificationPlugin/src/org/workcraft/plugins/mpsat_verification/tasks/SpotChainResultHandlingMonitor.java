package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.pcomp.tasks.PcompOutput;
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
        Result<? extends MpsatOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
        if (chainResult.isSuccess()) {
            MpsatOutput mpsatOutput = (mpsatResult == null) ? null : mpsatResult.getPayload();
            return new MpsatLtlxOutputInterpreter(we, mpsatOutput, interactive).interpret();
        }

        if (chainResult.isFailure()) {
            Result<? extends Ltl2tgbaOutput> ltl2tgbaResult = (chainOutput == null) ? null : chainOutput.getLtl2tgbaResult();
            // Process starter-sensitivity failure
            if ((mpsatResult == null) && (ltl2tgbaResult != null) && ltl2tgbaResult.isSuccess()) {
                Ltl2tgbaOutput ltl2tgbaOutput = ltl2tgbaResult.getPayload();
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
            message += ERROR_CAUSE_PREFIX + genericCause.getMessage();
        } else {
            SpotChainOutput chainOutput = chainResult.getPayload();
            Result<? extends Ltl2tgbaOutput> ltl2tgbaResult = (chainOutput == null) ? null : chainOutput.getLtl2tgbaResult();
            Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
            Result<? extends PcompOutput> pcompResult = (chainOutput == null) ? null : chainOutput.getPcompResult();
            Result<? extends MpsatOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
            if ((ltl2tgbaResult != null) && (ltl2tgbaResult.isFailure())) {
                message += "\n\nCould not derive B\u00FCchi automaton.";
                Throwable ltl2tgbaCause = ltl2tgbaResult.getCause();
                if (ltl2tgbaCause != null) {
                    message += ERROR_CAUSE_PREFIX + ltl2tgbaCause.getMessage();
                }
            } else  if ((exportResult != null) && (exportResult.isFailure())) {
                message += "\n\nCould not export the model as a .g file.";
                Throwable exportCause = exportResult.getCause();
                if (exportCause != null) {
                    message += ERROR_CAUSE_PREFIX + exportCause.getMessage();
                }
            } else if ((pcompResult != null) && (pcompResult.isFailure())) {
                message += "\n\nPcomp could not compose models.";
                Throwable pcompCause = pcompResult.getCause();
                if (pcompCause != null) {
                    message += ERROR_CAUSE_PREFIX + pcompCause.getMessage();
                } else {
                    PcompOutput pcompOutput = pcompResult.getPayload();
                    if (pcompOutput != null) {
                        String pcompError = pcompOutput.getErrorsHeadAndTail();
                        message += ERROR_CAUSE_PREFIX + pcompError;
                    }
                }
            } else if ((mpsatResult != null) && (mpsatResult.isFailure())) {
                message += "\n\nMPSat could not verify temporal property property.";
                Throwable mpsatCause = mpsatResult.getCause();
                if (mpsatCause != null) {
                    message += ERROR_CAUSE_PREFIX + mpsatCause.getMessage();
                } else {
                    MpsatOutput mpsatOutput = mpsatResult.getPayload();
                    if (mpsatOutput != null) {
                        String mpsatError = mpsatOutput.getErrorsHeadAndTail();
                        message += ERROR_CAUSE_PREFIX + mpsatError;
                    }
                }
            } else {
                message += "\n\nSPOT chain task returned failure status without further explanation.";
            }
        }
        return message;
    }

}
