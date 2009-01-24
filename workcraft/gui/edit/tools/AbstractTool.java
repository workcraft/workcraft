package org.workcraft.gui.edit.tools;

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
