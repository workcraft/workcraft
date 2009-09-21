package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface ParametricCurve {
	public Point2D getPointOnCurve (double t);
	public Point2D getNearestPointOnCurve(Point2D pt);
	public double getDistanceToCurve(Point2D pt);
	public Rectangle2D getBoundingBox();
}