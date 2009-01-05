package org.workcraft.gui.edit.tools;

import java.awt.Graphics2D;

import org.workcraft.gui.edit.graph.GraphEditor;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public interface GraphEditorTool {
	public void activated(GraphEditor editor);
	public void deactivated(GraphEditor editor);

	public void mouseMoved(GraphEditorMouseEvent e);
	public void mouseClicked(GraphEditorMouseEvent e);
	public void mouseEntered(GraphEditorMouseEvent e);
	public void mouseExited(GraphEditorMouseEvent e);
	public void mousePressed(GraphEditorMouseEvent e);
	public void mouseReleased(GraphEditorMouseEvent e);

	public void drawInUserSpace(GraphEditor editor, Graphics2D g);
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g);

	public String getName();
	public String getIconPath();
}
