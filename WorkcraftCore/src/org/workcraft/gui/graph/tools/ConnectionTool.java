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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.util.GUI;

public class ConnectionTool extends AbstractTool {
	static private final Color incompleteConnectionColor = Color.GREEN;
	static private final Color validConnectionColor = Color.BLUE;
	static private final Color invalidConnectionColor = Color.RED;

	private boolean forbidConnectingArcs = true;
	private boolean forbidSelfLoops = true;

	private Point2D mousePosition = null;
	private VisualNode firstNode = null;
	private VisualNode currentNode = null;
	private String warningMessage = null;
	private boolean mouseLeftFirstNode = false;

	private static Color highlightColor = new Color(99, 130, 191).brighter();

	public ConnectionTool () {
	}

	public ConnectionTool (boolean forbidConnectingArcs, boolean forbidSelfLoops) {
		this.forbidConnectingArcs = forbidConnectingArcs;
		this.forbidSelfLoops = forbidSelfLoops;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/connect.svg");
	}

	@Override
	public String getLabel() {
		return "Connect";
	}

	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_C;
	}

	private void resetState(GraphEditor editor) {
		mousePosition = null;
		firstNode = null;
		currentNode = null;
		warningMessage = null;
		mouseLeftFirstNode = false;
		editor.getModel().selectNone();
		editor.getWorkspaceEntry().setCanModify(true);
	}

	private void updateState(GraphEditor editor) {
		VisualNode node = (VisualNode) HitMan.hitTestForConnection(mousePosition, editor.getModel());
		if (!forbidConnectingArcs || !(node instanceof VisualConnection)) {
			currentNode = node;
			if (currentNode != firstNode) {
				mouseLeftFirstNode = true;
				warningMessage = null;
			}
		}
	}

	@Override
	public void activated(final GraphEditor editor) {
		super.activated(editor);
		resetState(editor);
	}


	@Override
	public void deactivated(final GraphEditor editor) {
		super.deactivated(editor);
		resetState(editor);
	}

	@Override
	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		if (firstNode != null && mousePosition != null) {
			g.setStroke(new BasicStroke((float)editor.getViewport().pixelSizeInUserSpace().getX()));
			Point2D center = TransformHelper.transform(firstNode, TransformHelper.getTransformToRoot(firstNode)).getCenter();
			Line2D line = new Line2D.Double(center.getX(), center.getY(), mousePosition.getX(), mousePosition.getY());
			if (currentNode == null) {
				g.setColor(incompleteConnectionColor);
				g.draw(line);
			} else {
				try {
					editor.getModel().validateConnection(firstNode, currentNode);
					g.setColor(validConnectionColor);
					g.draw(line);
				} catch (InvalidConnectionException e) {
					warningMessage = e.getMessage();
					g.setColor(invalidConnectionColor);
					g.draw(line);
				}
			}
		}
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		mousePosition = e.getPosition();
		updateState(e.getEditor());
		e.getEditor().repaint();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		mousePosition = e.getPosition();
		updateState(e.getEditor());
		if ((e.getButton() == MouseEvent.BUTTON1) && (currentNode != null)) {
			if (firstNode == null) {
				firstNode = currentNode;
				warningMessage = null;
				mouseLeftFirstNode = false;
				e.getEditor().getWorkspaceEntry().setCanModify(false);
			} else if ((firstNode == currentNode) && (forbidSelfLoops || !mouseLeftFirstNode)) {
				if (forbidSelfLoops) {
					warningMessage = "Self-loops are not allowed";
				} else if (!mouseLeftFirstNode) {
					warningMessage = "Move the mouse outside this node before creating a self-loop";
				}
			} else {
				try {
					e.getEditor().getWorkspaceEntry().saveMemento();
					e.getModel().connect(firstNode, currentNode);
					if ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0) {
						firstNode = currentNode;
						currentNode = null;
						mouseLeftFirstNode = false;
					} else {
						resetState(e.getEditor());
					}
				} catch (InvalidConnectionException exeption) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			resetState(e.getEditor());
		}
		e.getEditor().repaint();
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			resetState(e.getEditor());
			e.getEditor().repaint();
		}
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		if (warningMessage != null) {
			GUI.drawEditorMessage(editor, g, Color.RED, warningMessage);
		} else {
			String message;
			if (firstNode == null) {
				message = "Click on the first component";
			} else {
				message = "Click on the second component (control+click to connect continuously)";
			}
			GUI.drawEditorMessage(editor, g, Color.BLACK, message);
		}
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if(node == currentNode) {
					return new Decoration(){
						@Override
						public Color getColorisation() {
							return highlightColor;
						}
						@Override
						public Color getBackground() {
							return null;
						}
					};
				}
				return null;
			}
		};
	}

}
