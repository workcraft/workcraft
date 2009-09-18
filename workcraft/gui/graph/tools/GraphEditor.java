package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.Viewport;

public interface GraphEditor {
	public Viewport getViewport();
	public MainWindow getMainWindow();
	public VisualModel getModel();
	public int getWidth();
	public int getHeight();
	public void snap(Point2D pos);
	public void repaint();
	public boolean hasFocus();
}