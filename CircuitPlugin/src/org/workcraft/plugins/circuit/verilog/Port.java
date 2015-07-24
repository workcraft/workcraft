package org.workcraft.plugins.circuit.verilog;

public class Port {
	public enum Type {
		INPUT, OUTPUT
	};

	public final String name;
	public final Type type;

	public Port(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public boolean isInput() {
		return (type == Type.INPUT);
	}

	public boolean isOutput() {
		return (type == Type.OUTPUT);
	}

}
