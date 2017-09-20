package org.workcraft.plugins.circuit.genlib;

public class Gate {
    public final String name;
    public final double size;
    public final Function function;
    public final String seq;
    public final boolean primitive;

    public Gate(String name, double size, Function function, String seq, boolean primitive) {
        this.name = name;
        this.size = size;
        this.function = function;
        this.seq = seq;
        this.primitive = primitive;
    }

    public boolean isSequential() {
        return seq != null;
    }

    public boolean isPrimitive() {
        return primitive;
    }

}
