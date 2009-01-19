package org.workcraft.gui.edit.tools;

import java.awt.Graphics2D;

public interface GraphEditorTool {
	public void activated(IGraphEditor editor);
	public void deactivated(IGraphEditor editor);

	public GraphEditorMouseListener getMouseListener();
	public GraphEditorKeyListener getKeyListener();

	public void drawInUserSpace(IGraphEditor editor, Graphics2D g);
	public void drawInScreenSpace(IGraphEditor editor, Graphics2D g);

	public String getName();
	public String getIconPath();
}
