package org.workcraft.plugins.dfs.decorations;

import org.workcraft.gui.graph.tools.Decoration;

public interface CounterflowLogicDecoration extends Decoration {
    boolean isForwardComputed();
    boolean isBackwardComputed();
    boolean isForwardComputedExcited();
    boolean isBackwardComputedExcited();
}
