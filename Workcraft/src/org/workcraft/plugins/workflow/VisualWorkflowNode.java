package org.workcraft.plugins.workflow;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.VisualComponent;

public class VisualWorkflowNode extends VisualComponent {
	private Rectangle2D shape = new Rectangle2D.Double(-1,-1,1,1);

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return shape.contains(pointInLocalSpace);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return shape;

	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.draw(shape);
	}

}
