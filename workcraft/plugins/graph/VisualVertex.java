package org.workcraft.plugins.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.workcraft.dom.Component;
import org.workcraft.dom.visual.VisualComponent;

public class VisualVertex extends VisualComponent {
	private static double size = 1;

	public VisualVertex(Vertex vertex) {
		super(vertex);
	}


	@Override
	public void draw(Graphics2D g) {
		System.out.println("Piska!!!");
		Shape shape = new Ellipse2D.Double(getX(), getY(), size, size);
		g.setColor(Color.WHITE);
		g.fill(shape);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(0.1f));
		g.draw(shape);
	}
}
