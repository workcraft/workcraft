package org.workcraft.plugins.circuit.verilog;

public class Pin {
    public final String name;
    public final String netName;

    public Pin(String name, String netName) {
        this.name = name;
        this.netName = netName;
    }

}
