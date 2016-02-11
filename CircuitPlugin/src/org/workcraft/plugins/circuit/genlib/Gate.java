package org.workcraft.plugins.circuit.genlib;

public class Gate {
    public final String name;
    public final Function function;
    public final String seq;
    public final boolean primitive;

    public Gate(String name, Function function, String seq, boolean primitive) {
        this.name = name;
        this.function = function;
        this.seq = seq;
        this.primitive = primitive;
    }

    public boolean isSequential() {
        return seq != null;
    }

    public boolean isPrimititve() {
        return primitive;
    }

}
