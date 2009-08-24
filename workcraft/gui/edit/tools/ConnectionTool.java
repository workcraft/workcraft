package org.workcraft.gui.edit.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class ConnectionTool extends AbstractTool {
	private VisualNode mouseOverObject = null;
	private VisualNode first = null;

	private boolean mouseExitRequiredForSelfLoop = true;
	private boolean leftFirst = false;
	private Point2D lastMouseCoords;
	private String warningMessage = null;

	private static Color highlightColor = new Color(99, 130, 191).brighter();

	public ConnectionTool () {
		lastMouseCoords = new Point2D.Double();
	}

	public Ellipse2D getBoundingCircle(Rectangle2D boundingRect) {

		double w_2 = boundingRect.getWidth()/2;
		double h_2 = boundingRect.getHeight()/2;
		double r = Math.sqrt( w_2*w_2 + h_2*h_2);

		return new Ellipse2D.Double(boundingRect.getCenterX() - r, boundingRect.getCenterY() - r, r*2, r*2);
	}

	@Override
	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		g.setStroke(new BasicStroke((float)editor.getViewport().pixelSizeInUserSpace().getX()));

		if (first != null) {
			VisualGroup root = editor.getModel().getRoot();
			warningMessage = null;
			if (mouseOverObject != null) {
				try {
					editor.getModel().validateConnection(first, mouseOverObject);
					drawConnectingLine(g, root, Color.GREEN);
				} catch (InvalidConnectionException e) {
					warningMessage = e.getMessage();
					drawConnectingLine(g, root, Color.RED);
				}
			} else {
				drawConnectingLine(g, root, Color.BLUE);
			}
		}
	}

	private void drawConnectingLine(Graphics2D g, VisualGroup root, Color color) {
		g.setColor(color);

		Rectangle2D bb = first.getBoundingBoxInAncestorSpace(root);
		Point2D center = new Point2D.Double(bb.getCenterX(), bb.getCenterY());

		Line2D line = new Line2D.Double(center.getX(), center.getY(), lastMouseCoords.getX(), lastMouseCoords.getY());
		g.draw(line);
	}

	public String getIconPath() {
		return null;
	}

	public String getName() {
		return "Connect";
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		lastMouseCoords = e.getPosition();

		VisualNode newMouseOverObject = HitMan.hitDeepestNodeOfType(e.getPosition(), e.getModel().getRoot(), VisualComponent.class);
		if (newMouseOverObject == null)
			newMouseOverObject = HitMan.hitDeepestNodeOfType(e.getPosition(), e.getModel().getRoot(), VisualConnection.class);

		if (mouseOverObject != newMouseOverObject) {
			if (mouseOverObject != null) {
				mouseOverObject.clearColorisation();
			}
			if (newMouseOverObject != null)
				newMouseOverObject.setColorisation(highlightColor);
		}

		mouseOverObject = newMouseOverObject;

		if (!leftFirst && mouseExitRequiredForSelfLoop) {
			if (mouseOverObject == first)
				mouseOverObject = null;
			else
				leftFirst = true;
		}

		e.getEditor().repaint();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (first == null) {
				if (mouseOverObject != null) {
					first = mouseOverObject;
					first.setColorisation(highlightColor);
					leftFirst = false;
					mouseMoved(e);
				}
			} else if (mouseOverObject != null) {
				try {
					VisualConnection vcon = e.getModel().connect(first, mouseOverObject);

					if ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0) {
						first.clearColorisation();
						first = vcon.getSecond();
						first.setColorisation(highlightColor);
						mouseOverObject = null;
					} else {
						first.clearColorisation();
						first = null;
					}
				} catch (InvalidConnectionException e1) {
					Toolkit.getDefaultToolkit().beep();
				}

			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			if (first != null) {
				first.clearColorisation();
				first = null;
			}
			mouseOverObject = null;
		}

		e.getEditor().repaint();
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		String message;

		if (warningMessage != null)
			message = warningMessage;
		else
			if (first == null)
				message = "Click on the first component";
			else
				message = "Click on the second component (control+click to connect continuously)";

		Rectangle2D r = g.getFont().getStringBounds(message, g.getFontRenderContext());
		if (warningMessage!=null)
			g.setColor(Color.RED);
		else
			g.setColor(Color.BLUE);
		g.drawString (message, editor.getWidth()/2 - (int)r.getWidth()/2, editor.getHeight() - 20);
	}

	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_C;
	}
}
