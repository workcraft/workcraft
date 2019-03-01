package org.workcraft.plugins.atacs.tasks;

import org.workcraft.tasks.ExternalProcessOutput;

public class AtacsSynthesisOutput extends ExternalProcessOutput {

    private final String verilog;

    public AtacsSynthesisOutput(ExternalProcessOutput output, String verilog) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.verilog = verilog;
    }

    public String getVerilog() {
        return this.verilog;
    }

}
