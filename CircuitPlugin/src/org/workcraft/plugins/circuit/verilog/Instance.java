package org.workcraft.plugins.circuit.verilog;

import java.util.List;

public class Instance {
	public final String name;
	public final String moduleName;
	public final List<Pin> connections;
	public final boolean zeroDelay;

	public Instance(String name, String moduleName, List<Pin> connections, boolean zeroDelay) {
		this.name = name;
		this.moduleName = moduleName;
		this.connections = connections;
		this.zeroDelay = zeroDelay;
	}

}
