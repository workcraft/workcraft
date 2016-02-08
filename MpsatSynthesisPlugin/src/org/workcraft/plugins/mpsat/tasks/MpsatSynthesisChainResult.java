package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.MpsatSynthesisSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class MpsatSynthesisChainResult {
    private Result<? extends Object> exportResult;
    private Result<? extends ExternalProcessResult> punfResult;
    private Result<? extends ExternalProcessResult> mpsatResult;
    private MpsatSynthesisSettings mpsatSettings;
    private String message;

    public MpsatSynthesisChainResult(Result<? extends Object> exportResult,
            Result<? extends ExternalProcessResult> pcompResult,
            Result<? extends ExternalProcessResult> punfResult,
            Result<? extends ExternalProcessResult> mpsatResult,
            MpsatSynthesisSettings mpsatSettings, String message) {

        this.exportResult = exportResult;
        this.punfResult = punfResult;
        this.mpsatResult = mpsatResult;
        this.mpsatSettings = mpsatSettings;
        this.message = message;
    }

    public MpsatSynthesisChainResult(Result<? extends Object> exportResult,
            Result<? extends ExternalProcessResult> pcompResult,
            Result<? extends ExternalProcessResult> punfResult,
            Result<? extends ExternalProcessResult> mpsatResult,
            MpsatSynthesisSettings mpsatSettings) {

        this(exportResult, pcompResult, punfResult, mpsatResult, mpsatSettings, null);
    }

    public MpsatSynthesisSettings getMpsatSettings() {
        return mpsatSettings;
    }

    public Result<? extends Object> getExportResult() {
        return exportResult;
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
