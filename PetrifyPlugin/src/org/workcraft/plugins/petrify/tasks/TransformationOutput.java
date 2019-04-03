package org.workcraft.plugins.petrify.tasks;

import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.plugins.stg.StgModel;

public class TransformationOutput extends ExternalProcessOutput {

    private final StgModel stg;

    public TransformationOutput(ExternalProcessOutput output, StgModel stg) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stg = stg;
    }

    public StgModel getStg() {
        return stg;
    }

}
