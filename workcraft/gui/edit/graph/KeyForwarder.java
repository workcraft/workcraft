package org.workcraft.gui.edit.graph;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.events.GraphEditorKeyEvent;

public class KeyForwarder implements KeyListener {

	private GraphEditor editor;
	private SelectedToolProvider selectedToolProvider;

	public KeyForwarder(GraphEditor editor, SelectedToolProvider selectedToolProvider) {
		this.editor = editor;
		this.selectedToolProvider = selectedToolProvider;
	}

	@Override
	public void keyPressed(KeyEvent event) {
		selectedToolProvider.getSelectedTool().getKeyListener().keyPressed(new GraphEditorKeyEvent(editor, event));
	}

	@Override
	public void keyReleased(KeyEvent event) {
		selectedToolProvider.getSelectedTool().getKeyListener().keyReleased(new GraphEditorKeyEvent(editor, event));
	}

	@Override
	public void keyTyped(KeyEvent event) {
		selectedToolProvider.getSelectedTool().getKeyListener().keyTyped(new GraphEditorKeyEvent(editor, event));
	}
}
