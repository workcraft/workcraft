package org.workcraft.plugins.sdfs.decorations;

import org.workcraft.gui.graph.tools.Decoration;

public interface BinaryRegisterDecoration extends Decoration {
	public boolean isTrueExcited();
	public boolean isTrueMarked();
	public boolean isFalseExcited();
	public boolean isFalseMarked();
}
