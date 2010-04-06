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

import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class BoundingBoxHelper {

	public static Rectangle2D union(Rectangle2D rect1, Rectangle2D rect2)
	{
		if (rect1 == null) return rect2;
		if (rect2 == null) return rect1;

		Rectangle2D result = new Rectangle2D.Double();

		result.setRect(rect1);
		result.add(rect2);

		return result;
	}

	public static Rectangle2D mergeBoundingBoxes(Collection<Touchable> nodes) {
		Rectangle2D bb = null;
		for(Touchable node : nodes)
			bb = union(bb, node.getBoundingBox());
		return bb;
	}


}
