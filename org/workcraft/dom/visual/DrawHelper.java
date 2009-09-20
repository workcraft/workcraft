package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class DrawHelper {

	public static void drawArrowHead(Graphics2D g, Color color, Point2D headPosition, double orientation, double length, double width) {
		Path2D.Double arrowShape = new Path2D.Double();
		arrowShape.moveTo(-length, -width / 2);
		arrowShape.lineTo(-length, width / 2);
		arrowShape.lineTo(0,0);
		arrowShape.closePath();

		Rectangle2D arrowBounds = arrowShape.getBounds2D();
		arrowBounds.setRect(arrowBounds.getMinX()+0.05f, arrowBounds.getMinY(), arrowBounds.getWidth(), arrowBounds.getHeight());

		AffineTransform arrowTransform = new AffineTransform();
		arrowTransform.translate(headPosition.getX(), headPosition.getY());
		arrowTransform.rotate(orientation);

		Shape transformedArrowShape = arrowTransform.createTransformedShape(arrowShape);

		g.setColor(color);
		g.setStroke(new BasicStroke((float)width));
		g.fill(transformedArrowShape);
	}

}
