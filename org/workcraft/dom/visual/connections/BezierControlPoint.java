/**
 *
 */
package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.workcraft.dom.Node;

class BezierControlPoint extends ControlPoint {
	private Point2D origin;
	private Node parent;

	public BezierControlPoint(Node parent) {
		this.parent = parent;
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public void setParent(Node parent) {
		throw new RuntimeException("Node does not support reparenting");
	}

	public void update (Point2D origin) {
		this.origin = origin;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);

		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(0.02f));

		Line2D l = new Line2D.Double(0, 0, origin.getX(), origin.getY());
		g.draw(l);
	}
}