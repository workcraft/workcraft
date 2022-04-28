package org.workcraft.plugins.circuit.genlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GateInterface {

    private final String name;
    private final String output;
    private final List<String> inputs;

    public GateInterface(List<String> inputs, String output) {
        this("", inputs, output);
    }

    public GateInterface(String name, List<String> inputs, String output) {
        this.name = name == null ? "" : name;
        this.output = output;
        this.inputs = inputs == null ? new ArrayList<>() : new ArrayList<>(inputs);
    }

    public String getName() {
        return name;
    }

    public String getOutput() {
        return output;
    }

    public List<String> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    @Override
    public String toString() {
        return name + " (" + String.join(",", inputs) + ", " + output + ")";
    }

}
