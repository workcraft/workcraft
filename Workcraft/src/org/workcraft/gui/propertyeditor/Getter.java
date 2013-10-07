package org.workcraft.gui.propertyeditor;

public interface Getter<O, V> {
	public V eval(O object);
}
