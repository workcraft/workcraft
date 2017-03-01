package org.workcraft.plugins.wtg.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.Result;

public class WaverConversionResult {
    private final Result<? extends ExternalProcessResult> result;
    private final Stg stg;

    public WaverConversionResult(Result<? extends ExternalProcessResult> result, Stg stg) {
        this.result = result;
        this.stg = stg;
    }

    public  Result<? extends ExternalProcessResult> getResult() {
        return result;
    }

    public Stg getConversionResult() {
        return stg;
    }
}
