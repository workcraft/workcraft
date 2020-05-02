package org.workcraft.plugins.atacs.tasks;

import org.workcraft.plugins.circuit.verilog.VerilogModule;
import org.workcraft.tasks.ExternalProcessOutput;

public class AtacsOutput extends ExternalProcessOutput {

    private final VerilogModule verilogModule;

    public AtacsOutput(ExternalProcessOutput output, VerilogModule verilogModule) {
        super(output.getReturnCode(), output.getStdout(), output.getStderr());
        this.verilogModule = verilogModule;
    }

    public VerilogModule getVerilogModule() {
        return verilogModule;
    }

}
