package org.workcraft.plugins.circuit.genlib;

public class UnaryGateInterface {

    public final String name;
    public final String output;
    public final String input;

    public UnaryGateInterface(String name, String output, String input) {
        this.name = name;
        this.output = output;
        this.input = input;
    }

    @Override
    public String toString() {
        return name + " (" + input + ", " + output + ")";
    }

}
