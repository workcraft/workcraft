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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class VisualContact extends VisualComponent {

	VisualCircuitComponent parentComponent;

	public VisualContact(Contact component) {
		super(component);
		parentComponent = null;
	}

	public VisualCircuitComponent getParentConnection() {
//		return parentConnection;
		return null;
	}

	@Override
	public void draw(Graphics2D g) {
		drawLabelInLocalSpace(g);

		double size = 0.5;
		double strokeWidth = 0.05;


		Shape shape = new Rectangle2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.setStroke(new BasicStroke((float)strokeWidth));
		g.draw(shape);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}


	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	@Override
	public Collection<MathNode> getMathReferences() {
		// TODO Auto-generated method stub
		return null;
	}

}
