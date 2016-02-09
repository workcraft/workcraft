package org.workcraft.plugins.dfs.decorations;

import java.awt.Color;

import org.workcraft.gui.graph.tools.Decoration;

public interface CounterflowRegisterDecoration extends Decoration {
    public boolean isForwardExcited();
    public boolean isBackwardExcited();
    public boolean isOrMarked();
    public boolean isOrExcited();
    public boolean isAndMarked();
    public boolean isAndExcited();
    public Color getTokenColor();
}
