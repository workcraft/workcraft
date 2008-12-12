package org.workcraft.gui.edit.text;

import java.awt.BorderLayout;

import javax.swing.JTextArea;

import org.workcraft.gui.edit.EditorWindow;

@SuppressWarnings("serial")
public class TextEditorWindow extends EditorWindow {
	protected JTextArea content = null;

	public TextEditorWindow(String title) {
		super(title);

		content = new JTextArea();
		content.setWrapStyleWord(true);
		content.setLineWrap(true);
		setLayout(new BorderLayout());
		this.add(content, BorderLayout.CENTER);
	}
}
