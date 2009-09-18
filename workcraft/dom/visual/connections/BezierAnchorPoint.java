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

	boolean isFirst;

	public BezierAnchorPoint(ConnectionInfo parentConnection, boolean isFirst) {
		super(parentConnection);
		this.isFirst = isFirst;
	}

	@Override
	public void draw(Graphics2D g) {
		Point2D p;

		if(isFirst)
			p = connectionInfo.getPoint1();
		else
			p = connectionInfo.getPoint2();

		AffineTransform at = getParentToLocalTransform();

		at.transform(p, p);

		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(0.02f));

		Line2D l = new Line2D.Double(0, 0, p.getX(), p.getY());
		g.draw(l);

		super.draw(g);
	}
}