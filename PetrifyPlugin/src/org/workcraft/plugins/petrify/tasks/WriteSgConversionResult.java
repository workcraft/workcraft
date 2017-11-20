package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;

public class WriteSgConversionResult {
    private final Result<? extends ExternalProcessResult> result;
    private final Fst fst;

    public WriteSgConversionResult(Result<? extends ExternalProcessResult> result, Fst fst) {
        this.result = result;
        this.fst = fst;
    }

    public  Result<? extends ExternalProcessResult> getResult() {
        return result;
    }

    public Fst getConversionResult() {
        return fst;
    }
}
