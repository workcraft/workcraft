package org.workcraft.plugins.mpsat_synthesis.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    private final byte[] verilogBytes;
    private final byte[] stgBytes;

    public MpsatOutput(ExternalProcessOutput output, byte[] verilogBytes, byte[] stgBytes) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.verilogBytes = verilogBytes;
        this.stgBytes = stgBytes;
    }

    public byte[] getVerilogBytes() {
        return verilogBytes;
    }

    public byte[] getStgBytes() {
        return stgBytes;
    }

}
