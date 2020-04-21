package org.workcraft.plugins.petrify.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

public class TransformationOutput extends ExternalProcessOutput {

    private final byte[] stgBytes;

    public TransformationOutput(ExternalProcessOutput output, byte[] stgBytes) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stgBytes = stgBytes;
    }

    public byte[] getStgBytes() {
        return stgBytes;
    }

}
