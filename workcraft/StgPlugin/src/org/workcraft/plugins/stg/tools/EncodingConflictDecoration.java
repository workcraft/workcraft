package org.workcraft.plugins.stg.tools;

import org.workcraft.gui.tools.Decoration;

import java.awt.*;

public interface EncodingConflictDecoration extends Decoration {
    Color getCoreDencityColor();
    Color getSingleConflictOverlapColor();
    Color getSingleConflictCoreColor();
    Color[] getMultipleConflictColors();
}
