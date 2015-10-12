package org.workcraft.plugins.cpog.untangling;


public class Node {

	public enum Type {
		PLACE,
		TRANSITION
	};

	private final String label;
	private final Type type;

	public Node(String lable, Type type) {
		this.label = lable;
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public Type getType() {
		return type;
	}



}
