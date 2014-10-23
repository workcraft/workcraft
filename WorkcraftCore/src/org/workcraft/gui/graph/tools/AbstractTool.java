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

import javax.swing.Icon;
import javax.swing.JPanel;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public abstract class AbstractTool implements GraphEditorTool {

	@Override
	public void activated(final GraphEditor editor) {
		editor.forceRedraw();
	}

	@Override
	public void deactivated(final GraphEditor editor) {
	}

	@Override
	public VisualModel getUnderlyingModel(VisualModel model) {
		return model;
	}

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
	}

	@Override
	public JPanel getInterfacePanel() {
		return null;
	}

	@Override
	public void drawInScreenSpace(final GraphEditor editor, Graphics2D g) {
	}

	@Override
	public void drawInUserSpace(final GraphEditor editor, Graphics2D g) {
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent event) {
	}

	@Override
	public void keyReleased(GraphEditorKeyEvent event) {
	}

	@Override
	public void keyTyped(GraphEditorKeyEvent event) {
	}

	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseEntered(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseExited(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseReleased(GraphEditorMouseEvent e) {
	}

	@Override
	public void startDrag(GraphEditorMouseEvent e) {
	}

	@Override
	public void finishDrag(GraphEditorMouseEvent e) {
	}

	@Override
	public boolean isDragging() {
		return false;
	}

	@Override
	public int getHotKeyCode() {
		return -1; // undefined hotkey
	}

	@Override
	public Icon getIcon() {
		return null;
	}

}
