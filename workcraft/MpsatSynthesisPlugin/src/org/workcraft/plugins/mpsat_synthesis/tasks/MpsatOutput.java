package org.workcraft.plugins.mpsat_synthesis.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    private final byte[] stgOutput;
    private final byte[] verilogOutput;

    public MpsatOutput(ExternalProcessOutput output) {
        this(output, null, null);
    }

    public MpsatOutput(ExternalProcessOutput output, byte[] stgOutput, byte[] verilogOutput) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stgOutput = stgOutput;
        this.verilogOutput = verilogOutput;
    }

    public byte[] getStgOutput() {
        return stgOutput;
    }

    public byte[] getVerilogOutput() {
        return verilogOutput;
    }

}
