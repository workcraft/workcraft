package org.workcraft.gui.graph;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class GraphEditorFocusListener implements FocusListener {
	private GraphEditorPanel editor;

	public GraphEditorFocusListener(GraphEditorPanel editor) {
		this.editor = editor;
	}

	public void focusGained(FocusEvent e) {
		editor.focusGained();
	}

	public void focusLost(FocusEvent e) {
		editor.focusLost();
	}
}