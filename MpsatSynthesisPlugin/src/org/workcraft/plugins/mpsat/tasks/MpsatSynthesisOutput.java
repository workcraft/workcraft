package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class MpsatSynthesisOutput extends ExternalProcessOutput {

    private final byte[] stgOutput;
    private final byte[] verilogOutput;

    public MpsatSynthesisOutput(ExternalProcessOutput output) {
        this(output, null, null);
    }

    public MpsatSynthesisOutput(ExternalProcessOutput output, byte[] stgOutput, byte[] verilogOutput) {
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
