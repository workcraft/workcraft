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
import java.util.Set;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.EditorOverlay;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.workspace.WorkspaceEntry;

public interface GraphEditor {
    Viewport getViewport();
    MainWindow getMainWindow();
    EditorOverlay getOverlay();
    WorkspaceEntry getWorkspaceEntry();
    VisualModel getModel();
    int getWidth();
    int getHeight();
    Set<Point2D> getSnaps(VisualNode node);
    Point2D snap(Point2D pos, Set<Point2D> snaps);
    void repaint();
    void forceRedraw();
    boolean hasFocus();
    void requestFocus();

    void zoomIn();
    void zoomOut();
    void zoomDefault();
    void zoomFit();

    void panLeft();
    void panUp();
    void panRight();
    void panDown();
    void panCenter();

}
