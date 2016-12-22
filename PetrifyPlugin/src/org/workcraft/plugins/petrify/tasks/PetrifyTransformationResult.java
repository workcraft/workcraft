package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.Result;

public class PetrifyTransformationResult {
    private final Result<? extends ExternalProcessResult> petrifyResult;
    private final StgModel result;

    public PetrifyTransformationResult(Result<? extends ExternalProcessResult> petrifyResult, StgModel result) {
        this.petrifyResult = petrifyResult;
        this.result = result;
    }

    public  Result<? extends ExternalProcessResult> getPetrifyResult() {
        return petrifyResult;
    }

    public StgModel getResult() {
        return result;
    }
}
