package org.workcraft.plugins.circuit.genlib;

public class BinaryGateInterface {

    public final String name;
    public final String firstInput;
    public final String secondInput;
    public final String output;

    public BinaryGateInterface(String name, String firstInput, String secondInput, String output) {
        this.name = name;
        this.firstInput = firstInput;
        this.secondInput = secondInput;
        this.output = output;
    }

    @Override
    public String toString() {
        return name + " (" + firstInput + ", " + secondInput  + ", " + output + ")";
    }

}
