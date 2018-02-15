package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.Result;

public class PetrifyTransformationResult {
    private final Result<? extends ExternalProcessOutput> petrifyResult;
    private final StgModel result;

    public PetrifyTransformationResult(Result<? extends ExternalProcessOutput> petrifyResult, StgModel result) {
        this.petrifyResult = petrifyResult;
        this.result = result;
    }

    public  Result<? extends ExternalProcessOutput> getPetrifyResult() {
        return petrifyResult;
    }

    public StgModel getResult() {
        return result;
    }
}
