package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;

public class SpotChainOutput extends ChainOutput {

    private final Result<? extends Ltl2tgbaOutput> ltl2tgbaResult;

    public SpotChainOutput(Result<? extends Ltl2tgbaOutput> ltl2tgbaResult) {
        this(ltl2tgbaResult, null, null, null);
    }

    public SpotChainOutput(Result<? extends Ltl2tgbaOutput> ltl2tgbaResult,
            Result<? extends MpsatOutput> mpsatResult) {

        this(ltl2tgbaResult, null, null, mpsatResult);
    }

    public SpotChainOutput(Result<? extends Ltl2tgbaOutput> ltl2tgbaResult,
            Result<? extends ExportOutput> exportResult,
            Result<? extends PcompOutput> pcompResult,
            Result<? extends MpsatOutput> mpsatResult) {

        super(exportResult, pcompResult, mpsatResult);
        this.ltl2tgbaResult = ltl2tgbaResult;
    }

    public Result<? extends Ltl2tgbaOutput> getLtl2tgbaResult() {
        return ltl2tgbaResult;
    }

}
