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
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.Icon;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.Polyline;
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
	private LinkedList<Point2D> controlPoints = null;

	private static Color highlightColor = new Color(1.0f, 0.5f, 0.0f).brighter();

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
		if ((firstNode != null) && (mousePosition != null)) {
			g.setStroke(new BasicStroke((float)editor.getViewport().pixelSizeInUserSpace().getX()));
			Point2D center = TransformHelper.transform(firstNode, TransformHelper.getTransformToRoot(firstNode)).getCenter();
			Path2D path = new Path2D.Double();
			path.moveTo(center.getX(), center.getY());
			if (controlPoints != null) {
				for (Point2D point: controlPoints) {
					path.lineTo(point.getX(), point.getY());
				}
			}
			path.lineTo(mousePosition.getX(), mousePosition.getY());
			if (currentNode == null) {
				g.setColor(incompleteConnectionColor);
				g.draw(path);
			} else {
				try {
					editor.getModel().validateConnection(firstNode, currentNode);
					g.setColor(validConnectionColor);
					g.draw(path);
				} catch (InvalidConnectionException e) {
					warningMessage = e.getMessage();
					g.setColor(invalidConnectionColor);
					g.draw(path);
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
		GraphEditor editor = e.getEditor();
		updateState(editor);
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (currentNode == null) {
				if (firstNode != null) {
					Set<Point2D> snaps = new HashSet<Point2D>();
					if (controlPoints.isEmpty()) {
						Point2D center = TransformHelper.transform(firstNode, TransformHelper.getTransformToRoot(firstNode)).getCenter();
						snaps.add(center);
					} else {
						snaps.add(controlPoints.getLast());
					}
					Point2D snapPos = editor.snap(mousePosition, snaps);
					controlPoints.add(snapPos);
				}
			} else {
				if (firstNode == null) {
					startConnection();
					editor.getWorkspaceEntry().setCanModify(false);
				} else if ((firstNode == currentNode) && (forbidSelfLoops || !mouseLeftFirstNode)) {
					if (forbidSelfLoops) {
						warningMessage = "Self-loops are not allowed";
					} else if (!mouseLeftFirstNode) {
						warningMessage = "Move the mouse outside this node before creating a self-loop";
					}
				} else if ((firstNode instanceof VisualGroup) || (currentNode instanceof VisualGroup)) {
					warningMessage = "Connection with group element is not allowed";
				} else {
					editor.getWorkspaceEntry().saveMemento();
					finishConnection(e.getModel());
					if ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0) {
						startConnection();
					} else {
						resetState(editor);
					}
				}
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			resetState(editor);
		}
		editor.repaint();
	}

	private void startConnection() {
		firstNode = currentNode;
		currentNode = null;
		warningMessage = null;
		mouseLeftFirstNode = false;
		controlPoints = new LinkedList<Point2D>();
	}

	private void finishConnection(VisualModel model) {
		try {
			VisualConnection connection = model.connect(firstNode, currentNode);
			AffineTransform rootToConnectionTransform = TransformHelper.getTransform(model.getRoot(), connection);
			if (controlPoints != null) {
				ConnectionGraphic graphic = connection.getGraphic();
				if (graphic instanceof Polyline) {
					Polyline polyline = (Polyline)graphic;
					ListIterator<Point2D> pointIterator = controlPoints.listIterator(controlPoints.size());
					// Iterate in reverse
					while(pointIterator.hasPrevious()) {
						Point2D point = pointIterator.previous();
						rootToConnectionTransform.transform(point, point);
						polyline.insertControlPointInSegment(point, 0);
					}
				}
			}
		} catch (InvalidConnectionException exeption) {
			Toolkit.getDefaultToolkit().beep();
		}
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
				message = "Click on the first component.";
			} else {
				message = "Click on the second component or create a node point. Hold Ctrl to connect continuously.";
			}
			GUI.drawEditorMessage(editor, g, Color.BLACK, message);
		}
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if (node == currentNode) {
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
