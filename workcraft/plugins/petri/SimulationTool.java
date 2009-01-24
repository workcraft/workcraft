package org.workcraft.plugins.petri;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.gui.edit.tools.DummyKeyListener;
import org.workcraft.gui.edit.tools.GraphEditorKeyListener;
import org.workcraft.gui.edit.tools.GraphEditorMouseListener;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class SimulationTool implements GraphEditorTool, GraphEditorMouseListener {

	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		Rectangle2D r = g.getFont().getStringBounds("Simulation O_O;;", g.getFontRenderContext());
		g.setColor(Color.BLUE);
		g.drawString ("Simulation O_O;;", editor.getWidth()/2 - (int)r.getWidth()/2, editor.getHeight() - 20);
	}

	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		// TODO: Highlight enabled transitions

	}

	public String getIconPath() {
		return null;
	}

	public String getName() {
		return "Simulation";
	}

	public void deactivated(GraphEditor editor) {
		// TODO Auto-generated method stub

	}

	public void activated(GraphEditor editor) {
		// TODO Auto-generated method stub

	}

	public void mouseClicked(GraphEditorMouseEvent e) {
		// TODO: implement simulation
		// Selectable mouseOverObject = e.getModel().getRoot().hitObject(e.getPosition());
		// ^^^ this is how to get the object that was clicked
		//         then if ( mouseOverObject instanceof Transition ) try to fire
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

	@Override
	public GraphEditorKeyListener getKeyListener() {
		return DummyKeyListener.getInstance();
	}

	@Override
	public GraphEditorMouseListener getMouseListener() {
		return this;
	}


}