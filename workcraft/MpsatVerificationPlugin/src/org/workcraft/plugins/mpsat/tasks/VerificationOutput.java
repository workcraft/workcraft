package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.ExternalProcessOutput;

public class VerificationOutput extends ExternalProcessOutput {

    private final StgModel inputStg;
    private final StgModel outputStg;
    private final VerificationParameters verificationParameters;

    public VerificationOutput(ExternalProcessOutput output, StgModel inputStg, StgModel outputStg,
            VerificationParameters verificationParameters) {

        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.inputStg = inputStg;
        this.outputStg = outputStg;
        this.verificationParameters = verificationParameters;
    }

    public StgModel getInputStg() {
        return inputStg;
    }

    public StgModel getOutputStg() {
        return outputStg;
    }

    public VerificationParameters getVerificationParameters() {
        return verificationParameters;
    }
}
