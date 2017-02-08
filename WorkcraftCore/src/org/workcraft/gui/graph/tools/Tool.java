package org.workcraft.gui.graph.tools;

import java.awt.Cursor;

import javax.swing.Icon;

public interface Tool {
    String getLabel();
    Icon getIcon();
    int getHotKeyCode();
    Cursor getCursor();
    boolean requiresButton();
}
