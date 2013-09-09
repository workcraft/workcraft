package org.workcraft.plugins.dfs.decorations;

import org.workcraft.gui.graph.tools.Decoration;

public interface CounterflowRegisterDecoration extends Decoration {
	public boolean isForwardExcited();
	public boolean isBackwardExcited();
	public boolean isOrMarked();
	public boolean isOrMarkedExcited();
	public boolean isAndMarked();
	public boolean isAndMarkedExcited();
}
