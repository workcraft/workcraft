package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class BoundingBoxHelper {

    public static Rectangle2D copy(Rectangle2D rect) {
        if (rect == null) {
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
        for (Touchable node : nodes) {
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

    public static Rectangle2D move(Rectangle2D rect, double dx, double dy) {
        return new Rectangle2D.Double(rect.getX() + dx, rect.getY() + dy, rect.getWidth(), rect.getHeight());
    }

    public static Rectangle2D move(Rectangle2D rect, Point2D d) {
        return move(rect, d.getX(), d.getY());
    }

    public static Rectangle2D transform(Rectangle2D rect, AffineTransform transform) {
        if (rect == null) {
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
