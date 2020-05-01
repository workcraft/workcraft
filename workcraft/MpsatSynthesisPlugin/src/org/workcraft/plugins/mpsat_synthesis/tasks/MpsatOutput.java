package org.workcraft.plugins.mpsat_synthesis.tasks;

import org.workcraft.plugins.circuit.verilog.VerilogModule;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.ExternalProcessOutput;

public class MpsatOutput extends ExternalProcessOutput {

    private final VerilogModule verilogModule;
    private final Stg stg;

    public MpsatOutput(ExternalProcessOutput output, VerilogModule verilogModule, Stg stg) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.stg = stg;
        this.verilogModule = verilogModule;
    }

    public VerilogModule getVerilogModule() {
        return verilogModule;
    }

    public Stg getStg() {
        return stg;
    }

}
