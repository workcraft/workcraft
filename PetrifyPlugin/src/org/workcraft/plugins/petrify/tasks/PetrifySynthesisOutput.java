package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;

public class PetrifySynthesisOutput extends ExternalProcessOutput {

    private final String equations;
    private final String verilog;
    private final String log;
    private final String stg;

    public PetrifySynthesisOutput(ExternalProcessOutput output, String log, String equations, String verilog, String stg) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.log = log;
        this.equations = equations;
        this.verilog = verilog;
        this.stg = stg;
    }

    public String getLog() {
        return this.log;
    }

    public String getEquation() {
        return this.equations;
    }

    public String getVerilog() {
        return this.verilog;
    }

    public String getStg() {
        return this.stg;
    }

}
