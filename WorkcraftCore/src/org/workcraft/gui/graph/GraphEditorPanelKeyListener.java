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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.graph.tools.GraphEditorKeyListener;

class GraphEditorPanelKeyListener implements KeyListener {
	GraphEditorPanel editor;
	GraphEditorKeyListener forwardListener;

	public GraphEditorPanelKeyListener(GraphEditorPanel editor, GraphEditorKeyListener forwardListener) {
		this.editor = editor;
		this.forwardListener = forwardListener;
	}

	public void keyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				editor.getMainWindow().panLeft();
				break;
			case KeyEvent.VK_UP:
				editor.getMainWindow().panUp();
				break;
			case KeyEvent.VK_RIGHT:
				editor.getMainWindow().panRight();
				break;
			case KeyEvent.VK_DOWN:
				editor.getMainWindow().panDown();
				break;
			}
		} else {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_EQUALS:
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_ADD:
				editor.getMainWindow().zoomIn();
				break;
			case KeyEvent.VK_MINUS:
			case KeyEvent.VK_UNDERSCORE:
			case KeyEvent.VK_SUBTRACT:
				editor.getMainWindow().zoomOut();
				break;
			case KeyEvent.VK_MULTIPLY:
				editor.getMainWindow().zoomFit();
				break;
			case KeyEvent.VK_DIVIDE:
				editor.getMainWindow().panCenter();
				break;
			}
		}
		GraphEditorKeyEvent geke = new GraphEditorKeyEvent (editor, e);
		forwardListener.keyPressed(geke);

	}

	public void keyReleased(KeyEvent e) {
		GraphEditorKeyEvent geke = new GraphEditorKeyEvent (editor, e);
		forwardListener.keyReleased(geke);

	}

	public void keyTyped(KeyEvent e) {
		GraphEditorKeyEvent geke = new GraphEditorKeyEvent (editor, e);
		forwardListener.keyTyped(geke);
	}
}