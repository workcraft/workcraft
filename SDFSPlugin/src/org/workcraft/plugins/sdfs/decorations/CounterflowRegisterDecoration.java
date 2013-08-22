package org.workcraft.plugins.sdfs.decorations;

import org.workcraft.gui.graph.tools.Decoration;

public interface CounterflowRegisterDecoration extends Decoration {
	public boolean isForwardEnabled();
	public boolean isBackwardEnabled();
	public boolean isOrMarked();
	public boolean isAndMarked();
	public boolean isForwardEnabledExcited();
	public boolean isBackwardEnabledExcited();
	public boolean isOrMarkedExcited();
	public boolean isAndMarkedExcited();
}
