package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface Selectable {

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace);
	public boolean hitTestInParentSpace(Point2D pointInParentSpace);
	public boolean hitTestInUserSpace(Point2D pointInUserSpace);


	public Rectangle2D getBoundingBoxInLocalSpace();
	public Rectangle2D getBoundingBoxInParentSpace();
	public Rectangle2D getBoundingBoxInUserSpace();
}
