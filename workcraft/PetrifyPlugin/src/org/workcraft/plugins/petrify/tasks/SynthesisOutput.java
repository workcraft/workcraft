package org.workcraft.plugins.petrify.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

public class SynthesisOutput extends ExternalProcessOutput {

    private final byte[] verilogBytes;
    private final byte[] stgBytes;

    private String log;
    private String equations;

    public SynthesisOutput(ExternalProcessOutput output, byte[] verilogBytes, byte[] stgBytes) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.verilogBytes = verilogBytes;
        this.stgBytes = stgBytes;
    }

    public byte[] getVerilogBytes() {
        return this.verilogBytes;
    }

    public byte[] getStgBytes() {
        return this.stgBytes;
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
