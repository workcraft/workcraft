package org.workcraft.plugins.petrify.tasks;

import org.workcraft.plugins.circuit.verilog.VerilogModule;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.ExternalProcessOutput;

public class SynthesisOutput extends ExternalProcessOutput {

    private final VerilogModule verilogModule;
    private final Stg stg;

    private String log;
    private String equations;

    public SynthesisOutput(ExternalProcessOutput output, VerilogModule verilogModule, Stg stg) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.verilogModule = verilogModule;
        this.stg = stg;
    }

    public VerilogModule getVerilogModule() {
        return verilogModule;
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
