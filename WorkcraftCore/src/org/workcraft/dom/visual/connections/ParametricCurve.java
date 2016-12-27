package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface ParametricCurve {
    Point2D getPointOnCurve(double t);
    Point2D getNearestPointOnCurve(Point2D pt);
    double getDistanceToCurve(Point2D pt);
    Rectangle2D getBoundingBox();

    Point2D getDerivativeAt(double t);
    Point2D getSecondDerivativeAt(double t);
}
