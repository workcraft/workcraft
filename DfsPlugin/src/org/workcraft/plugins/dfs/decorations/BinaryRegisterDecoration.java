package org.workcraft.plugins.dfs.decorations;

import java.awt.Color;

import org.workcraft.gui.graph.tools.Decoration;

public interface BinaryRegisterDecoration extends Decoration {
    boolean isTrueExcited();
    boolean isTrueMarked();
    boolean isFalseExcited();
    boolean isFalseMarked();
    Color getTokenColor();
}
