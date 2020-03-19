package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.plugins.stg.StgModel;

public class VerificationOutput extends ExternalProcessOutput {

    private final StgModel inputStg;
    private final StgModel outputStg;

    public VerificationOutput(ExternalProcessOutput output) {
        this(output, null, null);
    }

    public VerificationOutput(ExternalProcessOutput output, StgModel inputStg, StgModel outputStg) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.inputStg = inputStg;
        this.outputStg = outputStg;
    }

    public StgModel getInputStg() {
        return inputStg;
    }

    public StgModel getOutputStg() {
        return outputStg;
    }

}
