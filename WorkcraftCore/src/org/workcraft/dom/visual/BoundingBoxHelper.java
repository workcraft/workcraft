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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class BoundingBoxHelper {

	public static Rectangle2D copy(Rectangle2D rect) {
		if(rect == null) {
    		return null;
		}
		Rectangle2D result = new Rectangle2D.Double();
		result.setRect(rect);
		return result;
	}

	public static Rectangle2D union(Rectangle2D rect1, Rectangle2D rect2) {
		if (rect1 == null) return copy(rect2);
		if (rect2 == null) return copy(rect1);

		Rectangle2D result = new Rectangle2D.Double();
		result.setRect(rect1);
		result.add(rect2);
		return result;
	}

	public static Rectangle2D mergeBoundingBoxes(Collection<Touchable> nodes) {
		Rectangle2D bb = null;
		for(Touchable node : nodes) {
			bb = union(bb, node.getBoundingBox());
		}
		return bb;
	}

	public static Rectangle2D expand(Rectangle2D rect, double x, double y) {
		Rectangle2D result = null;
		if (rect != null) {
			result = new Rectangle2D.Double();
			result.setRect(rect);
			x /= 2.0f;
			y /= 2.0f;
			result.add(rect.getMinX() - x, rect.getMinY() - y);
			result.add(rect.getMaxX() + x, rect.getMaxY() + y);
		}
		return result;
	}

	public static Rectangle2D move(Rectangle2D rect, double x, double y) {
		return new Rectangle2D.Double(rect.getX()+x, rect.getY()+y, rect.getWidth(), rect.getHeight());
	}

	public static Rectangle2D transform(Rectangle2D rect, AffineTransform transform) {
		if(rect == null) {
    		return null;
		}
		Point2D p0 = new Point2D.Double(rect.getMinX(), rect.getMinY());
		Point2D p1 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());

		transform.transform(p0, p0);
		transform.transform(p1, p1);

		Rectangle2D.Double result = new Rectangle2D.Double(p0.getX(), p0.getY(), 0, 0);
		result.add(p1);

		return result;
	}

}
