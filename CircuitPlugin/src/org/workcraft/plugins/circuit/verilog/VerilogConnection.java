package org.workcraft.plugins.circuit.verilog;

public class VerilogConnection {
    public final String name;
    public final String netName;
    public final Integer netIndex;

    public VerilogConnection(String name, String netName, Integer netIndex) {
        this.name = name;
        this.netName = netName;
        this.netIndex = netIndex;
    }

}
