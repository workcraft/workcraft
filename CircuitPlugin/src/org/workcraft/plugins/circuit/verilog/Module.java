package org.workcraft.plugins.circuit.verilog;

import java.util.List;

public class Module {
	public final String name;
	public final List<Port> ports;
	public final List<Instance> instances;

	public Module(String name, List<Port> ports, List<Instance> instances) {
		this.name = name;
		this.ports = ports;
		this.instances = instances;
	}

}
