package org.workcraft.gui.edit.graph;

import java.awt.Graphics;

import org.workcraft.gui.edit.EditorWindow;

public class GraphEditorWindow extends EditorWindow {
	protected GraphEditorPane editorPane;
	public GraphEditorWindow(String title) {
		super(title);
		editorPane = new GraphEditorPane();
		this.setContentPane(editorPane);
	}

}
