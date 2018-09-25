package org.workcraft.plugins.circuit.utils;

public class Gate3 {

    public final String name;
    public final String in1;
    public final String in2;
    public final String out;

    public Gate3(String name, String in1, String in2, String out) {
        this.name = name;
        this.in1 = in1;
        this.in2 = in2;
        this.out = out;
    }

    @Override
    public String toString() {
        return name + " (" + in1 + ", " + in2 + ", " + out + ")";
    }

}
