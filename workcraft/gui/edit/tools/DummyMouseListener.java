package org.workcraft.gui.edit.tools;

import org.workcraft.gui.events.GraphEditorMouseEvent;

public class DummyMouseListener implements GraphEditorMouseListener {

	private static DummyMouseListener instance = new DummyMouseListener();
	public static DummyMouseListener getInstance()
	{
		return instance;
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
}
