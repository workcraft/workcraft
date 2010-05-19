package org.workcraft.gui.trees;

import javax.swing.JPopupMenu;

public interface TreePopupProvider<T> {
	public JPopupMenu getPopup(final T path);
}