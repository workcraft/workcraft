package org.workcraft.plugins.circuit.verilog;

import java.util.List;
import java.util.Map;

public class VerilogModule {
    public final String name;
    public final List<VerilogPort> ports;
    public final List<VerilogAssign> assigns;
    public final List<VerilogInstance> instances;
    public final Map<VerilogNet, Boolean> initialState;

    public VerilogModule(String name, List<VerilogPort> ports, List<VerilogAssign> assigns,
            List<VerilogInstance> instances, Map<VerilogNet, Boolean> initialState) {

        this.name = name;
        this.ports = ports;
        this.assigns = assigns;
        this.instances = instances;
        this.initialState = initialState;
    }

    public boolean isEmpty() {
        return assigns.isEmpty() && instances.isEmpty();
    }

}
