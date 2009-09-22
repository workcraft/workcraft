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
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			editor.getViewport().pan(20, 0);
			editor.repaint();
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			editor.getViewport().pan(-20, 0);
			editor.repaint();
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_UP) {
			editor.getViewport().pan(0, 20);
			editor.repaint();
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			editor.getViewport().pan(0, -20);
			editor.repaint();
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_EQUALS) {
			editor.getViewport().zoom(1);
			editor.repaint();
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_MINUS) {
			editor.getViewport().zoom(-1);
			editor.repaint();
			return;
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