package org.workcraft.dom.visual;

import org.workcraft.utils.Geometry;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class TouchableTransformer implements Touchable {

    private final AffineTransform transformation;
    private final Touchable toTransform;
    private final AffineTransform inverseTransformation;

    public TouchableTransformer(Touchable toTransform, AffineTransform transformation) {
        this.toTransform = toTransform;
        this.transformation = transformation;
        this.inverseTransformation = Geometry.optimisticInverse(transformation);
    }

    @Override
    public Rectangle2D getBoundingBox() {
        Rectangle2D bb = toTransform.getBoundingBox();

        if (bb == null) {
            return null;
        }

        Point2D[] corners = new Point2D[4];
        corners[0] = new Point2D.Double(bb.getMinX(), bb.getMinY());
        corners[1] = new Point2D.Double(bb.getMaxX(), bb.getMinY());
        corners[2] = new Point2D.Double(bb.getMinX(), bb.getMaxY());
        corners[3] = new Point2D.Double(bb.getMaxX(), bb.getMaxY());

        transformation.transform(corners, 0, corners, 0, 4);

        double minX = corners[0].getX();
        double maxX = corners[0].getX();
        double minY = corners[0].getY();
        double maxY = corners[0].getY();
        for (int i = 1; i < corners.length; i++) {
            if (corners[i].getX() < minX) {
                minX = corners[i].getX();
            }
            if (corners[i].getX() > maxX) {
                maxX = corners[i].getX();
            }
            if (corners[i].getY() < minY) {
                minY = corners[i].getY();
            }
            if (corners[i].getY() > maxY) {
                maxY = corners[i].getY();
            }
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public boolean hitTest(Point2D point) {
        Point2D transformed = new Point2D.Double();
        inverseTransformation.transform(point, transformed);
        return toTransform.hitTest(transformed);
    }

    @Override
    public Point2D getCenter() {
        return transformation.transform(toTransform.getCenter(), null);
    }
}
