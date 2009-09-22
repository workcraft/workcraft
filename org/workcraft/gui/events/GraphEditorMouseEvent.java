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

package org.workcraft.gui.events;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.graph.tools.GraphEditor;

public class GraphEditorMouseEvent {

	GraphEditor editor;

	int event;
	Point2D position;
	int button;
	int clickCount;
	int modifiers;

	public GraphEditorMouseEvent(GraphEditor editor, MouseEvent e) {
		this.editor = editor;

		event = e.getID();
		if(editor!=null)
			position = editor.getViewport().screenToUser(e.getPoint());
		else
			position = new Point2D.Double(0, 0);
		button = e.getButton();
		clickCount = e.getClickCount();
		modifiers = e.getModifiersEx();
	}

	public GraphEditorMouseEvent(GraphEditor editor, int event, Point2D position, int button, int clickCount, int modifiers) {
		this.editor = editor;
		this.event = event;
		this.position = position;
		this.button = button;
		this.clickCount = clickCount;
		this.modifiers = modifiers;
	}

	public GraphEditor getEditor() {
		return editor;
	}

	public VisualModel getModel() {
		return editor.getModel();
	}

	public int getEvent() {
		return event;
	}

	public Point2D getPosition() {
		return position;
	}

	public double getX() {
		return position.getX();
	}

	public double getY() {
		return position.getY();
	}

	public int getButton() {
		return button;
	}

	public int getClickCount() {
		return clickCount;
	}

	public int getModifiers() {
		return modifiers;
	}
}
