package org.workcraft.gui.propertyeditor;

public interface Setter<O, V> {
	public void eval(O obj, V value);
}
