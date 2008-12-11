package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.graph.Vertex;

public class VisualTransition extends VisualComponent {
	private static double size = 1;
	private static float strokeWidth = 0.1f;

	public VisualTransition(Transition transition) {
		super(transition);
	}

	public VisualTransition(Transition transition, Element xmlElement) {
		super(transition, xmlElement);
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


	public Rectangle2D getBoundingBox() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}
}
