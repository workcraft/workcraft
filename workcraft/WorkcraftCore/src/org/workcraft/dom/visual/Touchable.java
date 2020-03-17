package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface Touchable {
    boolean hitTest(Point2D point);
    Rectangle2D getBoundingBox();
    Point2D getCenter();
}
