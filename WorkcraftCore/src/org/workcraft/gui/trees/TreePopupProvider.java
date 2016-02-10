package org.workcraft.gui.trees;

import javax.swing.JPopupMenu;

public interface TreePopupProvider<T> {
    JPopupMenu getPopup(final T path);
}
