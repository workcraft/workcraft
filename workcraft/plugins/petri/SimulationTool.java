package org.workcraft.plugins.petri;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

import org.workcraft.gui.edit.tools.AbstractTool;
import org.workcraft.gui.edit.tools.GraphEditor;

public class SimulationTool extends AbstractTool {

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		Rectangle2D r = g.getFont().getStringBounds("Simulation O_O;;", g.getFontRenderContext());
		g.setColor(Color.BLUE);
		g.drawString ("Simulation O_O;;", editor.getWidth()/2 - (int)r.getWidth()/2, editor.getHeight() - 20);
	}

	@Override
	public String getIconPath() {
		return null;
	}

	@Override
	public String getName() {
		return "Simulation";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_M;
	}
}