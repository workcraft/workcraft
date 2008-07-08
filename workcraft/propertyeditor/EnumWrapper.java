package org.workcraft.propertyeditor;

public class EnumWrapper {
	String string;
	int index;

	public EnumWrapper(int index, String string) {
		this.index = index;
		this.string = string;
	}

	public int index() {
		return index;
	}

	public String toString() {
		return string;
	}
}