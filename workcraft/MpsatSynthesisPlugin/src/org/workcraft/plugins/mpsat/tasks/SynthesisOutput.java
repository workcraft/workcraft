package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

public class SynthesisOutput extends ExternalProcessOutput {

    private final byte[] stgOutput;
    private final byte[] verilogOutput;

    public SynthesisOutput(ExternalProcessOutput output) {
        this(output, null, null);
    }

    public SynthesisOutput(ExternalProcessOutput output, byte[] stgOutput, byte[] verilogOutput) {
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
