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

package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.Coloriser;


public class ControlPoint extends VisualTransformableNode implements Drawable, Touchable {
	private double size = 0.15;
	private Color fillColor = Color.BLUE;

	public ControlPoint() {
		setHidden(true);
	}

	Shape shape = new Ellipse2D.Double(
			-size / 2,
			-size / 2,
			size,
			size);

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size, -size, size*2, size*2);
	}

	@Override
	public void draw(DrawRequest r) {
		r.getGraphics().setColor(Coloriser.colorise(fillColor, r.getDecoration().getColorisation()));
		r.getGraphics().fill(shape);
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	@Override
	public Point2D getCenterInLocalSpace()
	{
		return new Point2D.Double(0, 0);
	}

}
