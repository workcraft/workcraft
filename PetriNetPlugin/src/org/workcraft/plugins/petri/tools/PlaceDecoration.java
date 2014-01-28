package org.workcraft.plugins.petri.tools;

import java.awt.Color;

import org.workcraft.gui.graph.tools.Decoration;

public interface PlaceDecoration extends Decoration {
	public int getTokens();
	public Color getTokenColor();
}
