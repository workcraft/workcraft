package org.workcraft.gui.propertyeditor;

public class ChoiceWrapper {
	public Object value;
	public String text;

	public ChoiceWrapper(String text, Object value) {
		this.text = text;
		this.value = value;
	}

	public String toString() {
		return text;
	}
}
