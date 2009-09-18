package org.workcraft.gui.graph.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class NodeGeneratorTool extends AbstractTool {
	private NodeGenerator generator;
	protected int hotKeyCode;

	public NodeGeneratorTool (NodeGenerator generator) {
		this.generator = generator;
	}

	public Icon getIcon() {
		return generator.getIcon();
	}

	public String getLabel() {
		return generator.getLabel();
	}

	public void mousePressed(GraphEditorMouseEvent e) {
		try {
			generator.generate(e.getModel(), e.getPosition());
		} catch (NodeCreationException e1) {
			throw new RuntimeException (e1);
		}
	}

	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		String message = generator.getText();
		Rectangle2D r = g.getFont().getStringBounds(message, g.getFontRenderContext());
		g.setColor(Color.BLUE);
		g.drawString (message, editor.getWidth()/2 - (int)r.getWidth()/2, editor.getHeight() - 20);

	}

	public int getHotKeyCode() {
		return generator.getHotKeyCode();
	}
}
