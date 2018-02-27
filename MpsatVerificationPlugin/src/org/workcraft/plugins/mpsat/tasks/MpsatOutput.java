package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    private final byte[] stgInput;
    private final byte[] stgOutput;

    public MpsatOutput(ExternalProcessOutput output) {
        this(output, null, null);
    }

    public MpsatOutput(ExternalProcessOutput output, byte[] stgInput, byte[] stgOutput) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stgInput = stgInput;
        this.stgOutput = stgOutput;
    }

    public byte[] getStgInput() {
        return stgInput;
    }

    public byte[] getStgOutput() {
        return stgOutput;
    }

}
