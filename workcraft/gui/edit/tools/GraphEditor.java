package org.workcraft.gui.edit.tools;

import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.edit.graph.Viewport;

public interface GraphEditor {
	Viewport getViewport();
	VisualModel getModel();
	int getWidth();
	int getHeight();
	void snap(Point2D pos);
	void repaint();
	MainWindow getMainWindow();
	public boolean hasFocus();
}