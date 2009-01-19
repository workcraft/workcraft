package org.workcraft.gui.edit.tools;

import java.awt.Graphics2D;

import org.workcraft.gui.events.GraphEditorMouseEvent;

public interface GraphEditorTool {
	public void activated(IGraphEditor editor);
	public void deactivated(IGraphEditor editor);

	public void mouseMoved(GraphEditorMouseEvent e);
	public void mouseClicked(GraphEditorMouseEvent e);
	public void mouseEntered(GraphEditorMouseEvent e);
	public void mouseExited(GraphEditorMouseEvent e);
	public void mousePressed(GraphEditorMouseEvent e);
	public void mouseReleased(GraphEditorMouseEvent e);

	public void drawInUserSpace(IGraphEditor editor, Graphics2D g);
	public void drawInScreenSpace(IGraphEditor editor, Graphics2D g);

	public String getName();
	public String getIconPath();
}
