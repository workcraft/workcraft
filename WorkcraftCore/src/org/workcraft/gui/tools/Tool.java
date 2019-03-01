package org.workcraft.gui.tools;

import java.awt.Cursor;

import javax.swing.Icon;

import org.workcraft.workspace.WorkspaceEntry;

public interface Tool {
    String getLabel();
    Icon getIcon();
    int getHotKeyCode();
    Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown);
    boolean requiresButton();
    boolean isApplicableTo(WorkspaceEntry we);
}
