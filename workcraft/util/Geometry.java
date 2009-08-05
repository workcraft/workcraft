package org.workcraft.util;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

public class Geometry {

	public static Point2D lerp(Point2D p1, Point2D p2, double t)
	{
		return new Point2D.Double(p1.getX()*(1-t)+p2.getX()*t, p1.getY()*(1-t)+p2.getY()*t);
	}

	public static class CurveSplitResult
	{
		public CurveSplitResult(Point2D splitPoint, Point2D control1, Point2D control2)
		{
			this.splitPoint = splitPoint;
			this.control1 = control1;
			this.control2 = control2;
		}

		public Point2D splitPoint;
		public Point2D control1;
		public Point2D control2;
	}

	public static CurveSplitResult splitCubicCurve(CubicCurve2D curve, double t)
	{
		Point2D a1 = lerp(curve.getP1(), curve.getCtrlP1(), t);
		Point2D a2 = lerp(curve.getCtrlP1(), curve.getCtrlP2(), t);
		Point2D a3 = lerp(curve.getCtrlP2(), curve.getP2(), t);

		Point2D b1 = lerp(a1, a2, t);
		Point2D b2 = lerp(a2, a3, t);

		Point2D c = lerp(b1, b2, t);

		return new CurveSplitResult(c, b1, b2);
	}
}
