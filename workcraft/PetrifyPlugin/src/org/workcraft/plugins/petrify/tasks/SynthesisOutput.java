package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.ExternalProcessOutput;

public class SynthesisOutput extends ExternalProcessOutput {

    private final byte[] verilogBytes;
    private final Stg stg;

    private String log;
    private String equations;

    public SynthesisOutput(ExternalProcessOutput output, byte[] verilogBytes, Stg stg) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.verilogBytes = verilogBytes;
        this.stg = stg;
    }

    public byte[] getVerilogBytes() {
        return verilogBytes;
    }

    public Stg getStg() {
        return stg;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getLog() {
        return log;
    }

    public void setEquations(String equations) {
        this.equations = equations;
    }

    public String getEquation() {
        return equations;
    }

}
