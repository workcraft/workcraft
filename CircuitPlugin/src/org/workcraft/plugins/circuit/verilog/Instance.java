package org.workcraft.plugins.circuit.verilog;

import java.util.List;

public class Instance {
	public final String name;
	public final String moduleName;
	public final List<Pin> connections;

	public Instance(String name, String moduleName, List<Pin> connections) {
		this.name = name;
		this.moduleName = moduleName;
		this.connections = connections;
	}

}
