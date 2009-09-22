/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

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
