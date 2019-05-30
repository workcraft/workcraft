package org.workcraft.plugins.circuit.verilog;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class VerilogModule {
    public final String name;
    public final List<VerilogPort> ports;
    public final List<VerilogAssign> assigns;
    public final List<VerilogInstance> instances;
    public final Map<String, Boolean> signalStates;
    public final Set<List<VerilogInstance>> groups;

    public VerilogModule(String name, List<VerilogPort> ports, List<VerilogAssign> assigns,
            List<VerilogInstance> instances, Map<String, Boolean> signalStates, Set<List<VerilogInstance>> groups) {

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
