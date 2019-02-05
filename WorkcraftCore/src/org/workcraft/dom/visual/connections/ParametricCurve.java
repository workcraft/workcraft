package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

public interface ParametricCurve {
    Point2D getPointOnCurve(double t);
    Point2D getNearestPointOnCurve(Point2D pt);
    double getDistanceToCurve(Point2D pt);
    Set<Point2D> getIntersections(Rectangle2D rect);

    Point2D getDerivativeAt(double t);
    Point2D getSecondDerivativeAt(double t);
}
