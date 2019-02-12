package org.workcraft.plugins.circuit.verilog;

import java.util.List;

public class VerilogInstance {
    public final String name;
    public final String moduleName;
    public final List<VerilogConnection> connections;
    public final boolean zeroDelay;

    public VerilogInstance(String name, String moduleName, List<VerilogConnection> connections, boolean zeroDelay) {
        this.name = name;
        this.moduleName = moduleName;
        this.connections = connections;
        this.zeroDelay = zeroDelay;
    }

}
