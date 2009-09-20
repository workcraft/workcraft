/**
 *
 */
package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

class BezierAnchorPoint extends VisualConnectionAnchorPoint {
	private Point2D origin;

	public void update (Point2D origin) {
		this.origin = origin;
	}

	@Override
	public void draw(Graphics2D g) {
		AffineTransform at = getParentToLocalTransform();

		Point2D p = new Point2D.Double();

		at.transform(origin, p);

		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(0.02f));

		Line2D l = new Line2D.Double(0, 0, p.getX(), p.getY());
		g.draw(l);

		super.draw(g);
	}
}