package org.workcraft.plugins.punf.tasks;

import org.workcraft.tasks.Result;

public class SpotChainOutput {

    private final Result<? extends Ltl2tgbaOutput> ltl2tgbaResult;
    private final Result<? extends PunfOutput> punfResult;

    public SpotChainOutput(
            Result<? extends Ltl2tgbaOutput> ltl2tgbaResult,
            Result<? extends PunfOutput> punfResult) {

        this.ltl2tgbaResult = ltl2tgbaResult;
        this.punfResult = punfResult;
    }

    public Result<? extends Ltl2tgbaOutput> getLtl2tgbaResult() {
        return ltl2tgbaResult;
    }

    public Result<? extends PunfOutput> getPunfResult() {
        return punfResult;
    }

}
