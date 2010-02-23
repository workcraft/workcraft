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

import org.workcraft.gui.events.GraphEditorMouseEvent;

public interface GraphEditorMouseListener {
	public void mouseMoved(GraphEditorMouseEvent e);
	public void mouseClicked(GraphEditorMouseEvent e);
	public void mouseEntered(GraphEditorMouseEvent e);
	public void mouseExited(GraphEditorMouseEvent e);
	public void mousePressed(GraphEditorMouseEvent e);
	public void mouseReleased(GraphEditorMouseEvent e);

	public void startDrag(GraphEditorMouseEvent e);
	public void finishDrag(GraphEditorMouseEvent e);
	public boolean isDragging();
}
