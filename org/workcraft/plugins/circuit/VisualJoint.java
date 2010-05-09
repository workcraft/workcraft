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

package org.workcraft.plugins.circuit;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.gui.Coloriser;

public class VisualJoint extends VisualCircuitComponent {
	static double jointSize = 0.25;

	public VisualJoint(Joint joint) {
		super(joint);
	}

	@Override
	public void draw(Graphics2D g) {
//		drawLabelInLocalSpace(g);


		Shape shape = new Ellipse2D.Double(
				-jointSize / 2,
				-jointSize / 2,
				jointSize,
				jointSize);

//		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.fill(shape);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-jointSize/2, -jointSize/2, jointSize, jointSize);
	}


	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return pointInLocalSpace.distanceSq(0, 0) < jointSize*jointSize/4;
	}

}
