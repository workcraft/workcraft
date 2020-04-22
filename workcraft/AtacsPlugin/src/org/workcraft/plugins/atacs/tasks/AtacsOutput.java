package org.workcraft.plugins.atacs.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

public class AtacsOutput extends ExternalProcessOutput {

    private final byte[] verilogBytes;

    public AtacsOutput(ExternalProcessOutput output, byte[] verilogBytes) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.verilogBytes = verilogBytes;
    }

    public byte[] getVerilogBytes() {
        return this.verilogBytes;
    }

}
