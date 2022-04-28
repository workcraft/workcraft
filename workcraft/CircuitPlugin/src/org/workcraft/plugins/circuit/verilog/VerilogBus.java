package org.workcraft.plugins.circuit.verilog;

import org.workcraft.types.Triple;

public class VerilogBus extends Triple<String, Integer, Integer> {

    public VerilogBus(String name, Integer min, Integer max) {
        super(name, Math.min(min, max), Math.max(min, max));
    }

    public String getName() {
        return getFirst();
    }

    public Integer getMinIndex() {
        return getSecond();
    }

    public Integer getMaxIndex() {
        return getThird();
    }

}
