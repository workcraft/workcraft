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

import java.awt.Graphics2D;

import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public abstract class AbstractTool implements GraphEditorTool {
	public void activated (GraphEditor editor) {
	}

	public void deactivated (GraphEditor editor) {
	}

	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
	}

	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
	}

	public void keyPressed(GraphEditorKeyEvent event) {
	}

	public void keyReleased(GraphEditorKeyEvent event) {
	}

	public void keyTyped(GraphEditorKeyEvent event) {
	}

	public void mouseClicked(GraphEditorMouseEvent e) {
	}

	public void mouseEntered(GraphEditorMouseEvent e) {
	}

	public void mouseExited(GraphEditorMouseEvent e) {
	}

	public void mouseMoved(GraphEditorMouseEvent e) {
	}

	public void mousePressed(GraphEditorMouseEvent e) {
	}

	public void mouseReleased(GraphEditorMouseEvent e) {
	}

	public int getHotKeyCode() {
		return -1; // undefined hotkey
	}
}
