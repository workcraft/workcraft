package org.workcraft.plugins.interop;

import java.awt.geom.Dimension2D;

public interface DotExportNode
{
	String getName();
	Dimension2D getDimensions();
	String[] getOutgoingArcs();
}

