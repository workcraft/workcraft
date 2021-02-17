package org.workcraft.plugins.circuit.refinement;

import java.util.Collections;
import java.util.Set;

public class ComponentInterface {

    private final String name;
    private final Set<String> inputs;
    private final Set<String> outputs;

    public ComponentInterface(String name, Set<String> inputs, Set<String> outputs) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public String getName() {
        return name;
    }

    public Set<String> getInputs() {
        return inputs == null ? null : Collections.unmodifiableSet(inputs);
    }

    public Set<String> getOutputs() {
        return outputs == null ? null : Collections.unmodifiableSet(outputs);
    }

}