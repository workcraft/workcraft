package org.workcraft.plugins.circuit.genlib;

public class Gate {
	public final String name;
	public final Function function;
	public final String seq;

	public Gate(String name, Function function) {
		this.name = name;
		this.function = function;
		this.seq = null;
	}

	public Gate(String name, Function function, String seq) {
		this.name = name;
		this.function = function;
		this.seq = seq;
	}

	public boolean isSequential() {
		return (seq != null);
	}

}
