package org.workcraft.plugins.dfs.decorations;

import org.workcraft.gui.tools.Decoration;

public interface CounterflowLogicDecoration extends Decoration {
    boolean isForwardComputed();
    boolean isBackwardComputed();
    boolean isForwardComputedExcited();
    boolean isBackwardComputedExcited();
}
