package org.workcraft.dom.visual;

import java.awt.Graphics2D;

import org.workcraft.gui.graph.tools.Decoration;

public interface DrawRequest {
	public Graphics2D getGraphics();
	public Decoration getDecoration();
	public VisualModel getModel();
}
