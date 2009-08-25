package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.HierarchyNode;

public interface Touchable extends HierarchyNode, Hidable {
	public boolean hitTest(Point2D point);
	public Rectangle2D getBoundingBox();
}
