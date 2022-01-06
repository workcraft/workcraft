package org.workcraft.plugins.circuit.verilog;

public class VerilogConnection {
    public final String name;
    public final VerilogNet net;

    public VerilogConnection(String name, VerilogNet net) {
        this.name = name;
        this.net = net;
    }

}
