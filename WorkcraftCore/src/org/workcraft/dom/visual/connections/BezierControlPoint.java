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

/**
 *
 */
package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;

public class BezierControlPoint extends ControlPoint {
	private Point2D origin;
	private Node parent;

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public void setParent(Node parent) {
		this.parent = parent;
	}

	public void update(Point2D origin) {
		this.origin = origin;
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		g.setColor(Color.LIGHT_GRAY);
		g.setStroke(new BasicStroke(0.02f));
		Line2D l = new Line2D.Double(0, 0, origin.getX(), origin.getY());
		g.draw(l);
		super.draw(r);
	}

}
