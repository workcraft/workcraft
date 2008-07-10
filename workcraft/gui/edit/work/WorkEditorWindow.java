package org.workcraft.gui.edit.work;

import java.awt.Graphics;

import org.workcraft.gui.edit.EditorWindow;

public class WorkEditorWindow extends EditorWindow {
	protected EditorPane editorPane;
	public WorkEditorWindow(String title) {
		super(title);
		editorPane = new EditorPane();
		this.setContentPane(editorPane);
	}
}
