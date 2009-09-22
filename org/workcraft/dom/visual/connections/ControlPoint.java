package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.Coloriser;


public class ControlPoint extends VisualTransformableNode implements Drawable, Touchable {
	private double size = 0.15;
	private Color fillColor = Color.BLUE;

	Shape shape = new Ellipse2D.Double(
			-size / 2,
			-size / 2,
			size,
			size);

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size, -size, size*2, size*2);
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Coloriser.colorise(fillColor, getColorisation()));
		g.fill(shape);
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}
}
