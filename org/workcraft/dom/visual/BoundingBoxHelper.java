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
