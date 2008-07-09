package org.workcraft.dom;


public class Connection {
	protected Component first;
	protected Component second;

	public Component getFirst() {
		return first;
			}

	public Component getSecond() {
		return second;
	}

	public Connection (Component first, Component second) {
		this.first = first;
		this.second = second;
	}
}
