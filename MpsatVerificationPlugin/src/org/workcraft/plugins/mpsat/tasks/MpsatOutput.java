package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    private final byte[] netInput;
    private final byte[] compInput;
    private final byte[] stgOutput;

    public MpsatOutput(ExternalProcessOutput output) {
        this(output, null, null, null);
    }

    public MpsatOutput(ExternalProcessOutput output, byte[] netInput, byte[] compInput, byte[] stgOutput) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.netInput = netInput;
        this.compInput = compInput;
        this.stgOutput = stgOutput;
    }

    public byte[] getNetInput() {
        return netInput;
    }

    public byte[] getCompInput() {
        return compInput;
    }

    public byte[] getStgOutput() {
        return stgOutput;
    }

}
