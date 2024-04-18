package org.workcraft.dom.visual;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface RenderingResult {

    void draw(Graphics2D g);
    Rectangle2D getBoundingBox();

    default boolean hitTest(Point2D point) {
        return getBoundingBox().contains(point);
    }

}
