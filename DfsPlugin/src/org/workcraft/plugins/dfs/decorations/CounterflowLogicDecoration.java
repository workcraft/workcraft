package org.workcraft.plugins.dfs.decorations;

import org.workcraft.gui.graph.tools.Decoration;

public interface CounterflowLogicDecoration extends Decoration {
    public boolean isForwardComputed();
    public boolean isBackwardComputed();
    public boolean isForwardComputedExcited();
    public boolean isBackwardComputedExcited();
}
