package org.workcraft.plugins.circuit.verilog;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Module {
    public final String name;
    public final List<Port> ports;
    public final List<Assign> assigns;
    public final List<Instance> instances;
    public final Map<String, Boolean> signalStates;
    public final Set<List<Instance>> groups;

    public Module(String name, List<Port> ports, List<Assign> assigns, List<Instance> instances, Map<String, Boolean> signalStates, Set<List<Instance>> groups) {
        this.name = name;
        this.ports = ports;
        this.assigns = assigns;
        this.instances = instances;
        this.signalStates = signalStates;
        this.groups = groups;
    }

    public boolean isEmpty() {
        return assigns.isEmpty() && instances.isEmpty();
    }

}
