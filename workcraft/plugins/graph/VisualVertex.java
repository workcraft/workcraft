package org.workcraft.plugins.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.VisualComponent;

public class VisualVertex extends VisualComponent {
	private static double size = 1;
	private static float strokeWidth = 0.1f;

	public VisualVertex(Vertex vertex) {
		super(vertex);
	}


	@Override
	public void draw(Graphics2D g) {
		Shape shape = new Ellipse2D.Double(getX()-size/2+strokeWidth/2, getY()-size/2+strokeWidth/2,
				size-strokeWidth, size-strokeWidth);
		g.setColor(Color.WHITE);
		g.fill(shape);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(strokeWidth));
		g.draw(shape);
	}


	public Rectangle2D getBoundingBox() {
		return new Rectangle2D.Double(getX()-size/2, getY()-size/2, size, size);
	}
}
