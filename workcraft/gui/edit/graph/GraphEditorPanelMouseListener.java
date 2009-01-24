package org.workcraft.gui.edit.graph;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.edit.tools.ToolProvider;
import org.workcraft.gui.events.GraphEditorMouseEvent;

class GraphEditorPanelMouseListener implements MouseMotionListener, MouseListener, MouseWheelListener {
	protected GraphEditor editor;
	protected Point lastMouseCoords = new Point();
	protected boolean panDrag = false;
	private ToolProvider toolProvider;

	public GraphEditorPanelMouseListener(GraphEditor editor, ToolProvider toolProvider) {
		this.editor = editor;
		this.toolProvider = toolProvider;
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		Point currentMouseCoords = e.getPoint();
		if (panDrag) {
			editor.getViewport().pan(currentMouseCoords.x - lastMouseCoords.x,
					currentMouseCoords.y - lastMouseCoords.y);
			editor.repaint();
		} else {
			toolProvider.getTool().mouseMoved(new GraphEditorMouseEvent(editor, e));
		}
		lastMouseCoords = currentMouseCoords;
	}

	public void mouseClicked(MouseEvent e) {
		if (editor.hasFocus()) {
			if (e.getButton() != MouseEvent.BUTTON2)
				toolProvider.getTool().mouseClicked(new GraphEditorMouseEvent(editor, e));
		} else if (e.getButton() == MouseEvent.BUTTON1)
			editor.grantFocus();
	}

	public void mouseEntered(MouseEvent e) {
		if (editor.hasFocus())
			toolProvider.getTool().mouseEntered(new GraphEditorMouseEvent(editor, e));
	}

	public void mouseExited(MouseEvent e) {
		if (editor.hasFocus())
			toolProvider.getTool().mouseExited(new GraphEditorMouseEvent(editor, e));
	}

	public void mousePressed(MouseEvent e) {
		if (editor.hasFocus())
			if (e.getButton() == MouseEvent.BUTTON2)
				panDrag = true;
			else
				toolProvider.getTool()
						.mousePressed(new GraphEditorMouseEvent(editor, e));
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2)
			panDrag = false;
		else
			toolProvider.getTool()
					.mouseReleased(new GraphEditorMouseEvent(editor, e));
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		editor.getViewport().zoom(-e.getWheelRotation(), e.getPoint());
		editor.repaint();
	}
}


