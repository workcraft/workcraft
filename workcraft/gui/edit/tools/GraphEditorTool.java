package org.workcraft.gui.edit.tools;

import java.awt.Graphics2D;

public interface GraphEditorTool {
	public void activated(GraphEditor editor);
	public void deactivated(GraphEditor editor);

	public GraphEditorMouseListener getMouseListener();
	public GraphEditorKeyListener getKeyListener();

	public void drawInUserSpace(GraphEditor editor, Graphics2D g);
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g);

	public String getName();
	public String getIconPath();
}
