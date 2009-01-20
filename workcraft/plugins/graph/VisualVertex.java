package org.workcraft.plugins.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualComponentGroup;

public class VisualVertex extends VisualComponent {
	private static double size = 1;
	private static float strokeWidth = 0.1f;

	public VisualVertex(Vertex vertex, VisualComponentGroup parent) {
		super(vertex, parent);
	}

	public VisualVertex(Vertex vertex, Element xmlElement, VisualComponentGroup parent) {
		super(vertex, xmlElement, parent);
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {

		Shape shape = new Ellipse2D.Double(
				-size/2+strokeWidth/2,
				-size/2+strokeWidth/2,
				size-strokeWidth,
				size-strokeWidth);

		g.setStroke(new BasicStroke(strokeWidth));

		g.setColor(Color.WHITE);
		g.fill(shape);
		g.setColor(Color.BLACK);
		g.draw(shape);
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}

	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (pointInLocalSpace.distanceSq(0,0) < size * size)
			return 1;
		else
			return 0;
	}
}
