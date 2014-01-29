/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class DrawHelper {

	public static void drawArrowHead(Graphics2D g, Point2D headPosition, double orientation, double length, double width, Color color) {
		AffineTransform arrowTransform = new AffineTransform();
		arrowTransform.translate(headPosition.getX(), headPosition.getY());
		arrowTransform.rotate(orientation);

		Path2D.Double shape = new Path2D.Double();
		shape.moveTo(-length, -width / 2);
		shape.lineTo(-length, width / 2);
		shape.lineTo(0,0);
		shape.closePath();

		Shape transformedArrowShape = arrowTransform.createTransformedShape(shape);

		g.setColor(color);
		g.fill(transformedArrowShape);
	}

	public static void drawBubbleHead(Graphics2D g, Point2D headPosition, double orientation, double size, Color color, Stroke stroke) {
		double size2 = size/2;
		double x = headPosition.getX() - size2 * Math.cos(orientation);
		double y = headPosition.getY() - size2 * Math.sin(orientation);

		AffineTransform arrowTransform = new AffineTransform();
		arrowTransform.translate(x, y);
		Shape shape = new Ellipse2D.Double(-size2, -size2, size, size);
		Shape transformedShape = arrowTransform.createTransformedShape(shape);

		g.setColor(Color.WHITE);
		g.fill(transformedShape);
		g.setColor(color);
		g.setStroke(stroke);
		g.draw(transformedShape);
	}

}
