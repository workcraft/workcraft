package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.SynthesisParameters;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class SynthesisChainOutput {
    private final Result<? extends ExportOutput> exportResult;
    private final Result<? extends PunfOutput> punfResult;
    private final Result<? extends SynthesisOutput> mpsatResult;
    private final SynthesisParameters synthesisParameters;

    public SynthesisChainOutput(
            Result<? extends ExportOutput> exportResult,
            Result<? extends PunfOutput> punfResult,
            Result<? extends SynthesisOutput> mpsatResult,
            SynthesisParameters synthesisParameters) {

        this.exportResult = exportResult;
        this.punfResult = punfResult;
        this.mpsatResult = mpsatResult;
        this.synthesisParameters = synthesisParameters;
    }

    public Result<? extends ExportOutput> getExportResult() {
        return exportResult;
    }
    public Result<? extends PunfOutput> getPunfResult() {
        return punfResult;
    }

    public Result<? extends SynthesisOutput> getMpsatResult() {
        return mpsatResult;
    }

    public SynthesisParameters getSynthesisParameters() {
        return synthesisParameters;
    }

}
