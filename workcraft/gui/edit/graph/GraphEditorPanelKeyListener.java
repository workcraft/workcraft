package org.workcraft.gui.edit.graph;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.workcraft.gui.edit.tools.GraphEditorKeyListener;
import org.workcraft.gui.events.GraphEditorKeyEvent;

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