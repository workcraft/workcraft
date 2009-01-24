package org.workcraft.gui.events;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.edit.tools.GraphEditor;

public class GraphEditorKeyEvent {
	GraphEditor editor;
	char keyChar;
	int keyCode;
	int modifiers;
	public GraphEditorKeyEvent(GraphEditor editor, KeyEvent event)
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

	private boolean isMaskHit(int mask)
	{
		return (modifiers&mask) == mask;
	}

	public boolean isCtrlDown()
	{
		return isMaskHit(InputEvent.CTRL_DOWN_MASK);
	}

	public boolean isShiftDown()
	{
		return isMaskHit(InputEvent.SHIFT_DOWN_MASK);
	}

	public boolean isAltDown()
	{
		return isMaskHit(InputEvent.ALT_DOWN_MASK);
	}

	public int getModifiers()
	{
		return modifiers;
	}

	public GraphEditor getEditor()
	{
		return editor;
	}

	public VisualModel getModel() {
		return editor.getModel();
	}
}
