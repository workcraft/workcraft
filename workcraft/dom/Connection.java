package org.workcraft.dom;

import org.w3c.dom.Element;

public class Connection {
	protected Component first;
	protected Component second;
	protected int ID;

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
	}

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

	public Connection (Element xmlElement, AbstractGraphModel model) {

	}

}