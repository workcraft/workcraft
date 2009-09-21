package org.workcraft.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.ParametricCurve;
import org.workcraft.dom.visual.connections.PartialCurveInfo;
import org.workcraft.dom.visual.connections.VisualConnectionInfo;

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

	public static CurveSplitResult splitCubicCurve(CubicCurve2D curve, double t) {
		Point2D a1 = lerp(curve.getP1(), curve.getCtrlP1(), t);
		Point2D a2 = lerp(curve.getCtrlP1(), curve.getCtrlP2(), t);
		Point2D a3 = lerp(curve.getCtrlP2(), curve.getP2(), t);

		Point2D b1 = lerp(a1, a2, t);
		Point2D b2 = lerp(a2, a3, t);

		Point2D c = lerp(b1, b2, t);

		return new CurveSplitResult(c, b1, b2);
	}

	public static Point2D getPointOnCubicCurve (CubicCurve2D curve, double t) {
		Point2D a1 = lerp(curve.getP1(), curve.getCtrlP1(), t);
		Point2D a2 = lerp(curve.getCtrlP1(), curve.getCtrlP2(), t);
		Point2D a3 = lerp(curve.getCtrlP2(), curve.getP2(), t);

		Point2D b1 = lerp(a1, a2, t);
		Point2D b2 = lerp(a2, a3, t);

		return lerp(b1, b2, t);
	}

	public static AffineTransform optimisticInverse(AffineTransform transform)
	{
		try
		{
			return transform.createInverse();
		}
		catch(NoninvertibleTransformException ex)
		{
			throw new RuntimeException("Matrix inverse failed! Pessimists win :( ");
		}
	}


	public static double getBorderPointParameter (Touchable collisionNode, ParametricCurve curve, double tStart, double tEnd)
	{
		Point2D point = new Point2D.Double();

		while(Math.abs(tEnd-tStart) > 1e-6)
		{
			double t = (tStart + tEnd)*0.5;
			point = curve.getPointOnCurve(t);

			if (collisionNode.hitTest(point))
				tStart = t;
			else
				tEnd = t;
		}

		return tStart;
	}

	public static PartialCurveInfo buildConnectionCurveInfo (VisualConnectionInfo connectionInfo, ParametricCurve curve, double endCutoff) {
		PartialCurveInfo info = new PartialCurveInfo();
		info.tStart = getBorderPointParameter(connectionInfo.getFirstShape(), curve, 0, 1);
		info.tEnd = getBorderPointParameter(connectionInfo.getSecondShape(), curve, 1, endCutoff);

		info.arrowHeadPosition = curve.getPointOnCurve(info.tEnd);

		double dt = info.tEnd;
		double t = 0.0;

		Point2D pt = new Point2D.Double();

		double arrowLengthSq = connectionInfo.getArrowLength()*connectionInfo.getArrowLength();

		while(dt > 1e-6)
		{
			dt /= 2.0;
			t += dt;
			pt = curve.getPointOnCurve(t);

			if (info.arrowHeadPosition.distanceSq(pt) < arrowLengthSq)
				t-=dt;
		}

		info.tEnd = t;
		info.arrowOrientation = Math.atan2(info.arrowHeadPosition.getY() - pt.getY() , info.arrowHeadPosition.getX() - pt.getX());

		return info;
	}
}
