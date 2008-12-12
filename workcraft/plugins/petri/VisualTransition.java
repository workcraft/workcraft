package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualComponentGroup;

public class VisualTransition extends VisualComponent {
	private static double size = 1;
	private static float strokeWidth = 0.1f;

	public VisualTransition(Transition transition, VisualComponentGroup parent) {
		super(transition, parent);
	}

	public VisualTransition(Transition transition, Element xmlElement, VisualComponentGroup parent) {
		super(transition, xmlElement, parent);
	}


	@Override
	public void draw(Graphics2D g) {
		Shape shape = new Rectangle2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);
		g.setColor(Color.WHITE);
		g.fill(shape);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(strokeWidth));
		g.draw(shape);
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}
}
