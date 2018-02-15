package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;

public class WriteSgConversionResult {
    private final Result<? extends ExternalProcessOutput> result;
    private final Fst fst;

    public WriteSgConversionResult(Result<? extends ExternalProcessOutput> result, Fst fst) {
        this.result = result;
        this.fst = fst;
    }

    public  Result<? extends ExternalProcessOutput> getResult() {
        return result;
    }

    public Fst getConversionResult() {
        return fst;
    }
}
