package org.workcraft.dom;


@VisualClass("org.workcraft.dom.visual.connections.VisualConnection")
public class Connection extends MathNode {
	private Component first;
	private Component second;

	public Connection () {
	}

	public Connection (Component first, Component second) {
		super();
		setComponents(first, second);
	}

	final public Component getFirst() {
		return first;
	}

	final public Component getSecond() {
		return second;
	}

	final public void setComponents(Component first, Component second) {
		this.first = first;	 this.second = second;
	}
}