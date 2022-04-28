package org.workcraft.plugins.circuit.verilog;

import java.util.List;

public class VerilogConnection {
    public final String name;
    public final List<VerilogNet> nets;

    public VerilogConnection(String name, List<VerilogNet> nets) {
        this.name = name;
        this.nets = nets;
    }

}
