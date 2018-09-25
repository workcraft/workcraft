package org.workcraft.plugins.circuit.utils;

public class Gate2 {

    public final String name;
    public final String in;
    public final String out;

    public Gate2(String name, String in, String out) {
        this.name = name;
        this.in = in;
        this.out = out;
    }

    @Override
    public String toString() {
        return name + " (" + in + ", " + out + ")";
    }

}
