package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface Touchable extends HierarchyNode {
	public Touchable hitTest(Point2D point);
	public Rectangle2D getBoundingBox();
}
