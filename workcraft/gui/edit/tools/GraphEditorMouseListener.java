package org.workcraft.gui.edit.tools;

import org.workcraft.gui.events.GraphEditorMouseEvent;

public interface GraphEditorMouseListener {
	public void mouseMoved(GraphEditorMouseEvent e);
	public void mouseClicked(GraphEditorMouseEvent e);
	public void mouseEntered(GraphEditorMouseEvent e);
	public void mouseExited(GraphEditorMouseEvent e);
	public void mousePressed(GraphEditorMouseEvent e);
	public void mouseReleased(GraphEditorMouseEvent e);
}
