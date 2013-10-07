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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
	private VisualNode mouseOverObject = null;
	private VisualNode first = null;
	private boolean forbidConnectingArcs = true;
	private boolean requireMouseExitForSelfLoop = true;
	private boolean mouseLeftFirst = false;
	private Point2D lastMouseCoords = new Point2D.Double();
	private String warningMessage = null;

	private static Color highlightColor = new Color(99, 130, 191).brighter();

	public ConnectionTool () {

	}
	public ConnectionTool (boolean forbidConnectingArcs, boolean requireMouseExitForSelfLoop) {
		this.forbidConnectingArcs = forbidConnectingArcs;
		this.requireMouseExitForSelfLoop = requireMouseExitForSelfLoop;
	}

	public Ellipse2D getBoundingCircle(Rectangle2D boundingRect) {

		double w_2 = boundingRect.getWidth()/2;
		double h_2 = boundingRect.getHeight()/2;
		double r = Math.sqrt(w_2 * w_2 + h_2 * h_2);

		return new Ellipse2D.Double(boundingRect.getCenterX() - r, boundingRect.getCenterY() - r, r*2, r*2);
	}

	@Override
	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		g.setStroke(new BasicStroke((float)editor.getViewport().pixelSizeInUserSpace().getX()));

		if (first != null) {
			warningMessage = null;
			if (mouseOverObject != null) {
				try {
					editor.getModel().validateConnection(first, mouseOverObject);
					drawConnectingLine(g, Color.GREEN);
				} catch (InvalidConnectionException e) {
					warningMessage = e.getMessage();
					drawConnectingLine(g, Color.RED);
				}
			} else {
				drawConnectingLine(g, Color.BLUE);
			}
		}
	}

	private void drawConnectingLine(Graphics2D g, Color color) {
		g.setColor(color);

		Point2D center = TransformHelper.transform(first, TransformHelper.getTransformToRoot(first)).getCenter();

		Line2D line = new Line2D.Double(center.getX(), center.getY(), lastMouseCoords.getX(), lastMouseCoords.getY());
		g.draw(line);
	}

	public String getLabel() {
		return "Connect";
	}

	private void updateMouseOverObject(GraphEditorMouseEvent e) {
		VisualNode node = (VisualNode) HitMan.hitTestForConnection(e.getPosition(), e.getModel());
		if (!forbidConnectingArcs || !(node instanceof VisualConnection)) {
			mouseOverObject = node;
			if (!mouseLeftFirst && requireMouseExitForSelfLoop) {
				if (mouseOverObject == first)
					mouseOverObject = null;
				else
					mouseLeftFirst = true;
			}
		}
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		lastMouseCoords = e.getPosition();
		updateMouseOverObject(e);
		e.getEditor().repaint();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			updateMouseOverObject(e);
			if (mouseOverObject != null) {
				if (first == null) {
					first = mouseOverObject;
					mouseLeftFirst = false;
					mouseOverObject = null;
					e.getEditor().getWorkspaceEntry().setCanModify(false);
				} else {
					try {
						e.getEditor().getWorkspaceEntry().saveMemento();
						e.getModel().connect(first, mouseOverObject);
						if ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0) {
							first = mouseOverObject;
							mouseOverObject = null;
						} else {
							first = null;
							e.getEditor().getWorkspaceEntry().setCanModify(true);
						}
					} catch (InvalidConnectionException e1) {
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			first = null;
			warningMessage = null;
			e.getEditor().getWorkspaceEntry().setCanModify(true);
		}
		e.getEditor().repaint();
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			first = null;
			warningMessage = null;
			e.getEditor().getWorkspaceEntry().setCanModify(true);
		}
		e.getEditor().repaint();
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		String message;

		if (warningMessage != null)
			message = warningMessage;
		else
			if (first == null)
				message = "Click on the first component";
			else
				message = "Click on the second component (control+click to connect continuously)";

		GUI.drawEditorMessage(editor, g, warningMessage!=null ? Color.RED : Color.BLACK, message);
	}

	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_C;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/connect.svg");
	}

	@Override
	public void activated(GraphEditor editor) {
		super.activated(editor);
		editor.getModel().selectNone();
		editor.getWorkspaceEntry().setCanModify(true);
		first = null;
		mouseOverObject = null;
	}


	@Override
	public void deactivated(GraphEditor editor) {
		super.deactivated(editor);
		first = null;
		mouseOverObject = null;
	}

	@Override
	public Decorator getDecorator() {
		return new Decorator() {

			@Override
			public Decoration getDecoration(Node node) {
				if(node == mouseOverObject)
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
				return null;
			}

		};
	}
}
