package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class ChainOutput {

    private final Result<? extends ExportOutput> exportResult;
    private final Result<? extends PcompOutput> pcompResult;
    private final Result<? extends PunfOutput> punfResult;

    public ChainOutput(Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult) {

        this.exportResult = exportResult;
        this.pcompResult = pcompResult;
        this.punfResult = punfResult;
    }

    public Result<? extends ExportOutput> getExportResult() {
        return exportResult;
    }

    public Result<? extends PcompOutput> getPcompResult() {
        return pcompResult;
    }

    public Result<? extends PunfOutput> getPunfResult() {
        return punfResult;
    }

}
