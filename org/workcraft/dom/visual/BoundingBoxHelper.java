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

	private static Rectangle2D.Double addBoundingBox(Rectangle2D.Double rect, Touchable node)
	{
		Rectangle2D addedRect = node.getBoundingBox();

		if(addedRect == null)
			return rect;

		if(rect==null) {
			rect = new Rectangle2D.Double();
			rect.setRect(addedRect);
		}
		else
			rect.add(addedRect);

		return rect;
	}

	public static Rectangle2D mergeBoundingBoxes(Collection<Touchable> nodes) {
		Rectangle2D.Double bb = null;
		for(Touchable node : nodes)
			bb = addBoundingBox(bb, node);
		return bb;
	}

}
