package org.workcraft.plugins.mpsat_synthesis.tasks;

import org.workcraft.plugins.mpsat_synthesis.SynthesisMode;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class SynthesisChainOutput {

    private final Result<? extends ExportOutput> exportResult;
    private final Result<? extends MpsatOutput> mpsatResult;
    private final SynthesisMode synthesisMode;

    public SynthesisChainOutput(
            Result<? extends ExportOutput> exportResult,
            Result<? extends MpsatOutput> mpsatResult,
            SynthesisMode synthesisMode) {

        this.exportResult = exportResult;
        this.mpsatResult = mpsatResult;
        this.synthesisMode = synthesisMode;
    }

    public Result<? extends ExportOutput> getExportResult() {
        return exportResult;
    }

    public Result<? extends MpsatOutput> getMpsatResult() {
        return mpsatResult;
    }

    public SynthesisMode getSynthesisMode() {
        return synthesisMode;
    }

}
