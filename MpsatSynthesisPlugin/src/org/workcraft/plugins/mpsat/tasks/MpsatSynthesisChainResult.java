package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.MpsatSynthesisParameters;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;

public class MpsatSynthesisChainResult {
    private Result<? extends Object> exportResult;
    private Result<? extends ExternalProcessOutput> punfResult;
    private Result<? extends ExternalProcessOutput> mpsatResult;
    private MpsatSynthesisParameters mpsatSettings;
    private String message;

    public MpsatSynthesisChainResult(Result<? extends Object> exportResult,
            Result<? extends ExternalProcessOutput> pcompResult,
            Result<? extends ExternalProcessOutput> punfResult,
            Result<? extends ExternalProcessOutput> mpsatResult,
            MpsatSynthesisParameters mpsatSettings, String message) {

        this.exportResult = exportResult;
        this.punfResult = punfResult;
        this.mpsatResult = mpsatResult;
        this.mpsatSettings = mpsatSettings;
        this.message = message;
    }

    public MpsatSynthesisChainResult(Result<? extends Object> exportResult,
            Result<? extends ExternalProcessOutput> pcompResult,
            Result<? extends ExternalProcessOutput> punfResult,
            Result<? extends ExternalProcessOutput> mpsatResult,
            MpsatSynthesisParameters mpsatSettings) {

        this(exportResult, pcompResult, punfResult, mpsatResult, mpsatSettings, null);
    }

    public MpsatSynthesisParameters getMpsatSettings() {
        return mpsatSettings;
    }

    public Result<? extends Object> getExportResult() {
        return exportResult;
    }
    public Result<? extends ExternalProcessOutput> getPunfResult() {
        return punfResult;
    }

    public Result<? extends ExternalProcessOutput> getMpsatResult() {
        return mpsatResult;
    }

    public String getMessage() {
        return message;
    }

}
