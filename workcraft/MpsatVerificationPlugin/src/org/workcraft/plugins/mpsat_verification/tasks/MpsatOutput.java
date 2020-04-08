package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    private final StgModel stg;
    private final VerificationParameters verificationParameters;

    public MpsatOutput(ExternalProcessOutput output, StgModel stg,
            VerificationParameters verificationParameters) {

        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stg = stg;
        this.verificationParameters = verificationParameters;
    }

    public StgModel getStg() {
        return stg;
    }

    public VerificationParameters getVerificationParameters() {
        return verificationParameters;
    }
}
