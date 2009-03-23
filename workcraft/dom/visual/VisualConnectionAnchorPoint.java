package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.gui.Coloriser;

public class VisualConnectionAnchorPoint extends VisualTransformableNode {
	private int index;
	private double size = 0.25;
	private static Color fillColor = Color.BLUE.darker();

	Shape shape = new Rectangle2D.Double(
			-size / 2,
			-size / 2,
			size,
			size);

	public VisualConnectionAnchorPoint(int index) {
		this.index = index;
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}

	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (getBoundingBoxInLocalSpace().contains(pointInLocalSpace))
			return 1;
		else
			return 0;
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {
		g.setColor(Coloriser.colorise(fillColor, getColorisation()));
		g.fill(shape);
	}

	public int getIndex() {
		return index;
	}

}
