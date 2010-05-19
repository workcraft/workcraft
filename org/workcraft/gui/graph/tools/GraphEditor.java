/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.EditorOverlay;
import org.workcraft.gui.graph.Viewport;

public interface GraphEditor {
	public Viewport getViewport();
	public MainWindow getMainWindow();
	public EditorOverlay getOverlay();
	public VisualModel getModel();
	public int getWidth();
	public int getHeight();
	public Point2D snap(Point2D pos);
	public void repaint();
	public boolean hasFocus();
}