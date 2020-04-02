package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.VerificationParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    private final StgModel inputStg;
    private final StgModel outputStg;
    private final VerificationParameters verificationParameters;

    public MpsatOutput(ExternalProcessOutput output, StgModel inputStg, StgModel outputStg,
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
