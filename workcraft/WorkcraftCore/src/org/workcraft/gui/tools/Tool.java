package org.workcraft.gui.tools;

import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;

public interface Tool {
    String getLabel();
    Icon getIcon();
    int getHotKeyCode();
    Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown);
    boolean requiresButton();
    boolean isApplicableTo(WorkspaceEntry we);
}
