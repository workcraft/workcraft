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

    public VerilogModule(String name, List<VerilogPort> verilogPorts, List<VerilogAssign> verilogAssigns, List<VerilogInstance> verilogInstances, Map<String, Boolean> signalStates, Set<List<VerilogInstance>> groups) {
        this.name = name;
        this.ports = verilogPorts;
        this.assigns = verilogAssigns;
        this.instances = verilogInstances;
        this.signalStates = signalStates;
        this.groups = groups;
    }

    public boolean isEmpty() {
        return assigns.isEmpty() && instances.isEmpty();
    }

}
