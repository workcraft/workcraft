package org.workcraft.plugins.circuit.verilog;

public class VerilogAssign {
    public final VerilogNet net;
    public final String formula;

    public VerilogAssign(VerilogNet net, String formula) {
        this.net = net;
        this.formula = formula;
    }

}
