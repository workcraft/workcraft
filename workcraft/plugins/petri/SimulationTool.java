package org.workcraft.plugins.petri;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.Selectable;
import org.workcraft.gui.edit.graph.GraphEditor;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class SimulationTool implements GraphEditorTool {

	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		Rectangle2D r = g.getFont().getStringBounds("< Simulation >", g.getFontRenderContext());
		g.setColor(Color.BLUE);
		g.drawString ("< Simulation >", editor.getWidth()/2 - (int)r.getWidth()/2, editor.getHeight() - 20);
	}

	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
	}

	public String getIconPath() {
		return null;
	}

	public String getName() {
		return "Simulation";
	}

	public void mouseClicked(GraphEditorMouseEvent e) {
		Selectable mouseOverObject = e.getModel().getRoot().hitObject(e.getPosition());
	}

	public void mouseEntered(GraphEditorMouseEvent e) {
	}

	public void mouseExited(GraphEditorMouseEvent e) {
	}

	public void mouseMoved(GraphEditorMouseEvent e) {
	}

	public void mousePressed(GraphEditorMouseEvent e) {
	}

	public void mouseReleased(GraphEditorMouseEvent e) {
	}
}
