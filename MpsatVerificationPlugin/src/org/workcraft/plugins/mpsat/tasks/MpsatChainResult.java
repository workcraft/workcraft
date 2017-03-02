package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class MpsatChainResult {
    private Result<? extends Object> exportResult;
    private Result<? extends ExternalProcessResult> pcompResult;
    private Result<? extends ExternalProcessResult> punfResult;
    private Result<? extends ExternalProcessResult> mpsatResult;
    private MpsatParameters mpsatSettings;
    private String message;

    public MpsatChainResult(Result<? extends Object> exportResult,
            Result<? extends ExternalProcessResult> pcompResult,
            Result<? extends ExternalProcessResult> punfResult,
            Result<? extends ExternalProcessResult> mpsatResult,
            MpsatParameters mpsatSettings, String message) {

        this.exportResult = exportResult;
        this.pcompResult = pcompResult;
        this.punfResult = punfResult;
        this.mpsatResult = mpsatResult;
        this.mpsatSettings = mpsatSettings;
        this.message = message;
    }

    public MpsatChainResult(Result<? extends Object> exportResult,
            Result<? extends ExternalProcessResult> pcompResult,
            Result<? extends ExternalProcessResult> punfResult,
            Result<? extends ExternalProcessResult> mpsatResult,
            MpsatParameters mpsatSettings) {

        this(exportResult, pcompResult, punfResult, mpsatResult, mpsatSettings, null);
    }

    public MpsatParameters getMpsatSettings() {
        return mpsatSettings;
    }

    public Result<? extends Object> getExportResult() {
        return exportResult;
    }

    public Result<? extends ExternalProcessResult> getPcompResult() {
        return pcompResult;
    }

    public Result<? extends ExternalProcessResult> getPunfResult() {
        return punfResult;
    }

    public Result<? extends ExternalProcessResult> getMpsatResult() {
        return mpsatResult;
    }

    public String getMessage() {
        return message;
    }

}
