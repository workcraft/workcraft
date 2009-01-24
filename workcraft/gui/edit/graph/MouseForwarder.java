package org.workcraft.gui.edit.graph;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.events.GraphEditorMouseEvent;

class MouseForwarder implements MouseMotionListener, MouseListener, MouseWheelListener {
	GraphEditor editor;
	protected Point lastMouseCoords = new Point();
	protected boolean panDrag = false;
	private ToolProvider toolProvider;
	private Focusable focusProvider;

	MouseForwarder(GraphEditor editor, ToolProvider toolProvider, Focusable focusProvider) {
		this.editor = editor;
		this.toolProvider = toolProvider;
		this.focusProvider = focusProvider;
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	private GraphEditorTool getSelectedTool()
	{
		return toolProvider.getTool();
	}

	public void mouseMoved(MouseEvent e) {
		Point currentMouseCoords = e.getPoint();
		if (panDrag) {
			editor.getViewport().pan(currentMouseCoords.x - lastMouseCoords.x,
					currentMouseCoords.y - lastMouseCoords.y);
			editor.repaint();
		} else {
			getSelectedTool().getMouseListener()
					.mouseMoved(new GraphEditorMouseEvent(editor, e));
		}
		lastMouseCoords = currentMouseCoords;
	}

	public void mouseClicked(MouseEvent e) {
		if (focusProvider.hasFocus()) {
			if (e.getButton() != MouseEvent.BUTTON2)
				getSelectedTool().getMouseListener()
						.mouseClicked(new GraphEditorMouseEvent(editor, e));
		} else if (e.getButton() == MouseEvent.BUTTON1)
			focusProvider.grantFocus();
	}

	public void mouseEntered(MouseEvent e) {
		if (focusProvider.hasFocus())
			getSelectedTool().getMouseListener()
					.mouseEntered(new GraphEditorMouseEvent(editor, e));
	}

	public void mouseExited(MouseEvent e) {
		if (focusProvider.hasFocus())
			getSelectedTool().getMouseListener()
					.mouseExited(new GraphEditorMouseEvent(editor, e));
	}

	public void mousePressed(MouseEvent e) {
		if (focusProvider.hasFocus())
			if (e.getButton() == MouseEvent.BUTTON2)
				panDrag = true;
			else
				getSelectedTool().getMouseListener()
						.mousePressed(new GraphEditorMouseEvent(editor, e));
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2)
			panDrag = false;
		else
			getSelectedTool().getMouseListener()
					.mouseReleased(new GraphEditorMouseEvent(editor, e));
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		editor.getViewport().zoom(-e.getWheelRotation(), e.getPoint());
		editor.repaint();
	}
}

interface Focusable
{
	public boolean hasFocus();
	public void grantFocus();
	public void removeFocus();
}

interface ToolProvider
{
	public GraphEditorTool getTool();
}
