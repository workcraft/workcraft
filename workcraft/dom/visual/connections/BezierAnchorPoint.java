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

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModelEventDispatcher;

class BezierAnchorPoint extends VisualConnectionAnchorPoint {
	private VisualComponent parentComponent;

	public BezierAnchorPoint(VisualConnection parentConnection, VisualComponent parentC) {
		super(parentConnection);

		parentComponent = parentC;
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {
		Point2D p = parentComponent.getPosition();
		AffineTransform at = getParentToLocalTransform();

		at.transform(p, p);

		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(0.02f));

		Line2D l = new Line2D.Double(0, 0, p.getX(), p.getY());
		g.draw(l);

		super.drawInLocalSpace(g);
	}

	public void subscribeEvents(VisualModelEventDispatcher eventDispatcher) {

	}

	public void unsubscribeEvents(VisualModelEventDispatcher eventDispatcher) {

	}
}