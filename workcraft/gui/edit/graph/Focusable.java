package org.workcraft.gui.edit.graph;

public interface Focusable {
	public boolean hasFocus();
	public void grantFocus();
	public void removeFocus();
}