package org.workcraft.plugins.circuit.verilog;

import java.util.List;
import java.util.Set;

public class Module {
	public final String name;
	public final List<Port> ports;
	public final Set<Instance> instances;
	public final Set<String> highSignals;
	public final Set<Set<Instance>> groups;

	public Module(String name, List<Port> ports, Set<Instance> instances, Set<String> highSignals, Set<Set<Instance>> groups) {
		this.name = name;
		this.ports = ports;
		this.instances = instances;
		this.highSignals = highSignals;
		this.groups = groups;
	}

}
