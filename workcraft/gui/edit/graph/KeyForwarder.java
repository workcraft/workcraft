package org.workcraft.gui.edit.graph;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.events.GraphEditorKeyEvent;

public class KeyForwarder implements KeyListener {

	private GraphEditor editor;
	private ToolProvider selectedToolProvider;

	public KeyForwarder(GraphEditor editor, ToolProvider selectedToolProvider) {
		this.editor = editor;
		this.selectedToolProvider = selectedToolProvider;
	}

	@Override
	public void keyPressed(KeyEvent event) {
		selectedToolProvider.getTool().getKeyListener().keyPressed(new GraphEditorKeyEvent(editor, event));
	}

	@Override
	public void keyReleased(KeyEvent event) {
		selectedToolProvider.getTool().getKeyListener().keyReleased(new GraphEditorKeyEvent(editor, event));
	}

	@Override
	public void keyTyped(KeyEvent event) {
		selectedToolProvider.getTool().getKeyListener().keyTyped(new GraphEditorKeyEvent(editor, event));
	}
}
