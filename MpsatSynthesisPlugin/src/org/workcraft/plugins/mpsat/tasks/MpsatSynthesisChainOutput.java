package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.MpsatSynthesisParameters;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class MpsatSynthesisChainOutput {
    private final Result<? extends ExportOutput> exportResult;
    private final Result<? extends PunfOutput> punfResult;
    private final Result<? extends MpsatSynthesisOutput> mpsatResult;
    private final MpsatSynthesisParameters mpsatSettings;

    public MpsatSynthesisChainOutput(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PunfOutput> punfResult,
            Result<? extends MpsatSynthesisOutput> mpsatResult,
            MpsatSynthesisParameters mpsatSettings) {

        this.exportResult = exportResult;
        this.punfResult = punfResult;
        this.mpsatResult = mpsatResult;
        this.mpsatSettings = mpsatSettings;
    }

    public Result<? extends ExportOutput> getExportResult() {
        return exportResult;
    }
    public Result<? extends PunfOutput> getPunfResult() {
        return punfResult;
    }

    public Result<? extends MpsatSynthesisOutput> getMpsatResult() {
        return mpsatResult;
    }

    public MpsatSynthesisParameters getMpsatSettings() {
        return mpsatSettings;
    }

}
