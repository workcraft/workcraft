package org.workcraft.plugins.circuit.verilog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerilogModule {
    public final String name;
    public final List<VerilogPort> ports;
    public final List<VerilogAssign> assigns;
    public final List<VerilogInstance> instances;
    public final Map<VerilogNet, Boolean> initialState;

    private final Map<String, VerilogPort> nameToPortMap = new HashMap<>();

    public VerilogModule(String name, List<VerilogPort> ports, List<VerilogAssign> assigns,
            List<VerilogInstance> instances, Map<VerilogNet, Boolean> initialState) {

        this.name = name;
        this.ports = ports;
        this.assigns = assigns;
        this.instances = instances;
        this.initialState = initialState;

        for (VerilogPort port : ports) {
            nameToPortMap.putIfAbsent(port.name, port);
        }
    }

    public boolean isEmpty() {
        return assigns.isEmpty() && instances.isEmpty();
    }

    public VerilogPort getPort(String name) {
        return nameToPortMap.get(name);
    }

}
