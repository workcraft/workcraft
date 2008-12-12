package org.workcraft.gui.edit.graph;


import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.edit.EditorWindow;

@SuppressWarnings("serial")
public class GraphEditorWindow extends EditorWindow {
	protected GraphEditorPane editorPane;

	public GraphEditorWindow(String title, VisualModel document) {
		super(title);
		//	this.editorPane = new GraphEditorPane(document);
		setContentPane(editorPane);
	}

}
