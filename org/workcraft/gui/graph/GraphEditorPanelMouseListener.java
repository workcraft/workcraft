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

package org.workcraft.gui.graph;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.ToolProvider;

class GraphEditorPanelMouseListener implements MouseMotionListener, MouseListener, MouseWheelListener {
	protected GraphEditor editor;
	protected Point lastMouseCoords = new Point();
	protected boolean panDrag = false;
	private ToolProvider toolProvider;

	public GraphEditorPanelMouseListener(GraphEditor editor, ToolProvider toolProvider) {
		this.editor = editor;
		this.toolProvider = toolProvider;
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		Point currentMouseCoords = e.getPoint();
		if (panDrag) {
			editor.getViewport().pan(currentMouseCoords.x - lastMouseCoords.x,
					currentMouseCoords.y - lastMouseCoords.y);
			editor.repaint();
		} else {
			GraphEditorTool tool = toolProvider.getTool();
			if (tool != null)
				tool.mouseMoved(new GraphEditorMouseEvent(editor, e));
		}
		lastMouseCoords = currentMouseCoords;
	}

	public void mouseClicked(MouseEvent e) {
		if (!editor.hasFocus())
			editor.getMainWindow().requestFocus((GraphEditorPanel)editor);

		if (e.getButton() != MouseEvent.BUTTON2)
			toolProvider.getTool().mouseClicked(new GraphEditorMouseEvent(editor, e));
	}

	public void mouseEntered(MouseEvent e) {
		if (editor.hasFocus()) {
			GraphEditorTool tool = toolProvider.getTool();
			if (tool != null)
				tool.mouseEntered(new GraphEditorMouseEvent(editor, e));
		}
	}

	public void mouseExited(MouseEvent e) {
		if (editor.hasFocus())
			toolProvider.getTool().mouseExited(new GraphEditorMouseEvent(editor, e));
	}

	public void mousePressed(MouseEvent e) {
		if (!editor.hasFocus())
			editor.getMainWindow().requestFocus((GraphEditorPanel)editor);

		if (e.getButton() == MouseEvent.BUTTON2)
			panDrag = true;
		else {
			GraphEditorTool tool = toolProvider.getTool();
			if (tool != null)
				tool.mousePressed(new GraphEditorMouseEvent(editor, e));
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2)
			panDrag = false;
		else {
			GraphEditorTool tool = toolProvider.getTool();
			if (tool != null)
				tool.mouseReleased(new GraphEditorMouseEvent(editor, e));
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		editor.getViewport().zoom(-e.getWheelRotation(), e.getPoint());
		editor.repaint();
	}
}


