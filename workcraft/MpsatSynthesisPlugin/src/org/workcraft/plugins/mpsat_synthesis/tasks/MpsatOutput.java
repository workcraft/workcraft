package org.workcraft.plugins.mpsat_synthesis.tasks;

import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    private final byte[] verilogBytes;
    private final Stg stg;

    public MpsatOutput(ExternalProcessOutput output, byte[] verilogBytes, Stg stg) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stg = stg;
        this.verilogBytes = verilogBytes;
    }

    public byte[] getVerilogBytes() {
        return verilogBytes;
    }

    public Stg getStg() {
        return stg;
    }

}
