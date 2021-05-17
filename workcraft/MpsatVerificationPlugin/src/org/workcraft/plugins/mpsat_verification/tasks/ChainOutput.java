package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class ChainOutput {

    private final Result<? extends ExportOutput> exportResult;
    private final Result<? extends PcompOutput> pcompResult;
    private final Result<? extends MpsatOutput> mpsatResult;

    public ChainOutput(Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends MpsatOutput> mpsatResult) {

        this.exportResult = exportResult;
        this.pcompResult = pcompResult;
        this.mpsatResult = mpsatResult;
    }

    public Result<? extends ExportOutput> getExportResult() {
        return exportResult;
    }

    public Result<? extends PcompOutput> getPcompResult() {
        return pcompResult;
    }

    public Result<? extends MpsatOutput> getMpsatResult() {
        return mpsatResult;
    }

}
