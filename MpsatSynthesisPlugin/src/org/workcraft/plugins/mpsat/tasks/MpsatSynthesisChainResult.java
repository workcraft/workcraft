package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.MpsatSynthesisParameters;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;

public class MpsatSynthesisChainResult {
    private final Result<? extends ExportOutput> exportResult;
    private final Result<? extends PunfOutput> punfResult;
    private final Result<? extends ExternalProcessOutput> mpsatResult;
    private final MpsatSynthesisParameters mpsatSettings;

    public MpsatSynthesisChainResult(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PunfOutput> punfResult,
            Result<? extends ExternalProcessOutput> mpsatResult,
            MpsatSynthesisParameters mpsatSettings) {

        this.exportResult = exportResult;
        this.punfResult = punfResult;
        this.mpsatResult = mpsatResult;
        this.mpsatSettings = mpsatSettings;
    }

    public MpsatSynthesisParameters getMpsatSettings() {
        return mpsatSettings;
    }

    public Result<? extends ExportOutput> getExportResult() {
        return exportResult;
    }
    public Result<? extends PunfOutput> getPunfResult() {
        return punfResult;
    }

    public Result<? extends ExternalProcessOutput> getMpsatResult() {
        return mpsatResult;
    }

}
