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
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import javax.swing.Icon;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.util.GUI;

public class NodeGeneratorTool extends AbstractTool {
	private final NodeGenerator generator;
	private VisualNode lastGeneratedNode = null;
	private String warningMessage = null;

	public NodeGeneratorTool (NodeGenerator generator) {
		this.generator = generator;
	}

	@Override
	public Icon getIcon() {
		return generator.getIcon();
	}

	@Override
	public String getLabel() {
		return generator.getLabel();
	}

	@Override
	public int getHotKeyCode() {
		return generator.getHotKeyCode();
	}

	private void resetState(GraphEditor editor) {
		lastGeneratedNode = null;
		warningMessage = null;
		editor.getModel().selectNone();
}

	@Override
	public void activated(GraphEditor editor) {
		super.activated(editor);
		resetState(editor);
	}


	@Override
	public void deactivated(GraphEditor editor) {
		super.deactivated(editor);
		resetState(editor);
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		if (lastGeneratedNode != null) {
			if (!lastGeneratedNode.hitTest(e.getPosition())) {
				resetState(e.getEditor());
				e.getEditor().repaint();
			}
		}
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		if (lastGeneratedNode != null) {
			warningMessage = "Move the mouse outside this node before creating a new node";
			e.getEditor().repaint();
		} else {
			try {
				if (e.getButton() == MouseEvent.BUTTON1) {
					e.getEditor().getWorkspaceEntry().saveMemento();
					lastGeneratedNode = generator.generate(e.getModel(), e.getEditor().snap(e.getPosition()));
				}
			} catch (NodeCreationException e1) {
				throw new RuntimeException (e1);
			}
		}
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		if (warningMessage != null) {
			GUI.drawEditorMessage(editor, g, Color.RED, warningMessage);
		} else {
			GUI.drawEditorMessage(editor, g, Color.BLACK, generator.getText());
		}
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return Decorator.Empty.INSTANCE;
	}

}
