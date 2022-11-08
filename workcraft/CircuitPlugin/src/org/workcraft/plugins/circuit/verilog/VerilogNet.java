package org.workcraft.plugins.circuit.verilog;

import org.workcraft.types.Pair;

public class VerilogNet extends Pair<String, Integer> {

    public VerilogNet(String name) {
        this(name, null);
    }

    public VerilogNet(String name, Integer index) {
        super(name, index);
    }

    public String getName() {
        return getFirst();
    }

    public Integer getIndex() {
        return getSecond();
    }

}
