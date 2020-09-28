package org.workcraft.plugins.circuit.genlib;

public class UnaryGateInterface {

    public final String name;
    public final String input;
    public final String output;

    public UnaryGateInterface(String name, String input, String output) {
        this.name = name;
        this.input = input;
        this.output = output;
    }

    @Override
    public String toString() {
        return name + " (" + input + ", " + output + ")";
    }

}
