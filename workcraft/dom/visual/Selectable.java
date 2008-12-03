package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface Selectable {
	public abstract boolean hitTest(Point2D point);
	public abstract Rectangle2D getBoundingBox();
}
