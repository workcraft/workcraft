package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.punf.tasks.Ltl2tgbaOutput;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class SpotChainOutput extends ChainOutput {

    private final Result<? extends Ltl2tgbaOutput> ltl2tgbaResult;

    public SpotChainOutput(Result<? extends Ltl2tgbaOutput> ltl2tgbaResult,
            Result<? extends PunfOutput> punfResult) {

        this(ltl2tgbaResult, null, null, punfResult);
    }

    public SpotChainOutput(Result<? extends Ltl2tgbaOutput> ltl2tgbaResult,
            Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends PunfOutput> punfResult) {

        super(exportResult, pcompResult, punfResult, null);
        this.ltl2tgbaResult = ltl2tgbaResult;
    }

    public Result<? extends Ltl2tgbaOutput> getLtl2tgbaResult() {
        return ltl2tgbaResult;
    }

}
