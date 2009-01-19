package org.workcraft.gui.events;

import java.awt.event.KeyEvent;

import org.workcraft.gui.edit.tools.IGraphEditor;

public class GraphEditorKeyEvent {
	IGraphEditor editor;
	char keyChar;
	int keyCode;
	int modifiers;
	public GraphEditorKeyEvent(IGraphEditor editor, KeyEvent event)
	{
		this.editor = editor;

		keyChar = event.getKeyChar();
		keyCode = event.getKeyCode();
		modifiers = event.getModifiersEx();
	}

	public char getKeyChar()
	{
		return keyChar;
	}

	public int getKeyCode()
	{
		return keyCode;
	}

	public int getModifiers()
	{
		return modifiers;
	}

	public IGraphEditor getEditor()
	{
		return editor;
	}
}
