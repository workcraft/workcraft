package org.workcraft.dom;

public class IDGenerator {
	private int IDCounter = 0;

	public int getNextID() {
		return IDCounter++;
	}

}
