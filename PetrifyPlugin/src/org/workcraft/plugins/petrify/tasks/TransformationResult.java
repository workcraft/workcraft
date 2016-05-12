package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.Result;

public class TransformationResult {
    private Result<? extends ExternalProcessResult> petrifyResult;
    private StgModel result;

    public TransformationResult(Result<? extends ExternalProcessResult> petrifyResult, StgModel result) {
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
