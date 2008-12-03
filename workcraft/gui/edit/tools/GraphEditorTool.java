package org.workcraft.gui.edit.tools;

import java.awt.Graphics2D;

import org.workcraft.gui.edit.graph.GraphEditorPane;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public interface GraphEditorTool {

	public void mouseMoved(GraphEditorMouseEvent e);
	public void mouseClicked(GraphEditorMouseEvent e);
	public void mouseEntered(GraphEditorMouseEvent e);
	public void mouseExited(GraphEditorMouseEvent e);
	public void mousePressed(GraphEditorMouseEvent e);
	public void mouseReleased(GraphEditorMouseEvent e);

	public void draw(GraphEditorPane editor, Graphics2D g);

}
