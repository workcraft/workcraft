package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    private final byte[] stgBytes;
    private final VerificationParameters verificationParameters;

    public MpsatOutput(ExternalProcessOutput output, byte[] stgBytes,
            VerificationParameters verificationParameters) {

        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stgBytes = stgBytes;
        this.verificationParameters = verificationParameters;
    }

    public byte[] getStgBytes() {
        return stgBytes;
    }

    public VerificationParameters getVerificationParameters() {
        return verificationParameters;
    }
}
