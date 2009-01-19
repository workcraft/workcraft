package org.workcraft.gui.edit.tools;

import org.workcraft.gui.events.GraphEditorKeyEvent;

public class DummyKeyListener implements GraphEditorKeyListener {

	private static DummyKeyListener instance = new DummyKeyListener();
	public static DummyKeyListener getInstance()
	{
		return instance;
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
}
