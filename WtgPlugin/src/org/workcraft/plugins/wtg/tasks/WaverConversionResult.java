package org.workcraft.plugins.wtg.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.Result;

public class WaverConversionResult {
    private final Result<? extends ExternalProcessOutput> result;
    private final Stg stg;

    public WaverConversionResult(Result<? extends ExternalProcessOutput> result, Stg stg) {
        this.result = result;
        this.stg = stg;
    }

    public  Result<? extends ExternalProcessOutput> getResult() {
        return result;
    }

    public Stg getConversionResult() {
        return stg;
    }
}
