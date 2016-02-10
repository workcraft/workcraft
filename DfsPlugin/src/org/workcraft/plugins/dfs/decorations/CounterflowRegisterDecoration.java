package org.workcraft.plugins.dfs.decorations;

import java.awt.Color;

import org.workcraft.gui.graph.tools.Decoration;

public interface CounterflowRegisterDecoration extends Decoration {
    boolean isForwardExcited();
    boolean isBackwardExcited();
    boolean isOrMarked();
    boolean isOrExcited();
    boolean isAndMarked();
    boolean isAndExcited();
    Color getTokenColor();
}
