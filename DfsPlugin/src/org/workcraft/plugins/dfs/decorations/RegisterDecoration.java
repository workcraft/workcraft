package org.workcraft.plugins.dfs.decorations;

import java.awt.Color;

import org.workcraft.gui.graph.tools.Decoration;

public interface RegisterDecoration extends Decoration {
    boolean isExcited();
    boolean isMarked();
    Color getTokenColor();
}
