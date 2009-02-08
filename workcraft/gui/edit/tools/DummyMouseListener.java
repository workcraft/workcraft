package org.workcraft.gui.edit.tools;

import org.workcraft.gui.events.GraphEditorMouseEvent;

public class DummyMouseListener implements GraphEditorMouseListener {

	private static DummyMouseListener instance = new DummyMouseListener();
	public static DummyMouseListener getInstance()
	{
		return instance;
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
}
