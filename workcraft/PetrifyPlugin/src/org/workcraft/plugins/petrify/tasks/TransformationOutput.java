package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.ExternalProcessOutput;

public class TransformationOutput extends ExternalProcessOutput {

    private final Stg stg;

    public TransformationOutput(ExternalProcessOutput output, Stg stg) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stg = stg;
    }

    public Stg getStg() {
        return stg;
    }

}
