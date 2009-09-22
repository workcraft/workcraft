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

package org.workcraft.testing.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;

class SquareNode extends VisualComponent
{
	Rectangle2D.Double rectOuter;
	Rectangle2D.Double rectInner;
	int resultToReturn;
	public SquareNode(VisualGroup parent, Rectangle2D.Double rectOuter, Rectangle2D.Double rectInner) {
		super(null);
		this.rectOuter = rectOuter;
		this.rectInner = rectInner;
	}

	public SquareNode(VisualGroup parent, Rectangle2D.Double rect) {
		this(parent, rect, rect);
	}

	@Override
	public String toString() {
		return rectInner.toString();
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return rectOuter;
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return rectInner.contains(pointInLocalSpace);
	}

	@Override
	public Collection<MathNode> getMathReferences() {
		// TODO Auto-generated method stub
		return null;
	}
}

