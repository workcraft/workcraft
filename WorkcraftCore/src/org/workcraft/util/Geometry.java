package org.workcraft.util;

import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.ParametricCurve;
import org.workcraft.dom.visual.connections.PartialCurveInfo;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;

import java.awt.geom.*;
import java.util.HashSet;
import java.util.Set;

public class Geometry {

    public static Point2D lerp(Point2D p1, Point2D p2, double t) {
        return new Point2D.Double(p1.getX() * (1 - t) + p2.getX() * t, p1.getY() * (1 - t) + p2.getY() * t);
    }

    public static Point2D middle(Point2D p1, Point2D p2) {
        return lerp(p1, p2, 0.5);
    }

    public static Point2D add(Point2D p1, Point2D p2) {
        Point2D result = (Point2D) p1.clone();
        result.setLocation(result.getX() + p2.getX(), result.getY() + p2.getY());
        return result;
    }

    public static Point2D subtract(Point2D p1, Point2D p2) {
        Point2D result = (Point2D) p1.clone();
        result.setLocation(result.getX() - p2.getX(), result.getY() - p2.getY());
        return result;
    }

    public static Point2D rotate90CCW(Point2D p) {
        Point2D result = (Point2D) p.clone();
        result.setLocation(-p.getY(), p.getX());
        return result;
    }

    public static Point2D normalize(Point2D p) {
        Point2D result = (Point2D) p.clone();
        double length = p.distance(0, 0);
        if (length < 0.0000001) {
            result.setLocation(0, 0);
        } else {
            result.setLocation(p.getX() / length, p.getY() / length);
        }
        return result;
    }

    public static Point2D reduce(Point2D p) {
        return multiply(normalize(p), Math.pow(p.distanceSq(0, 0), 0.2));
    }

    public static double dotProduct(Point2D v1, Point2D v2) {
        return v1.getX() * v2.getX() + v1.getY() * v2.getY();
    }

    public static Point2D multiply(Point2D p, double a) {
        Point2D result = (Point2D) p.clone();
        result.setLocation(p.getX() * a, p.getY() * a);
        return result;
    }

    public static class CurveSplitResult {
        public final CubicCurve2D curve1;
        public final CubicCurve2D curve2;

        public CurveSplitResult(CubicCurve2D curve1, CubicCurve2D curve2) {
            this.curve1 = curve1;
            this.curve2 = curve2;
        }
    }

    public static CubicCurve2D buildCurve(Point2D p1, Point2D cp1, Point2D cp2, Point2D p2) {
        return new CubicCurve2D.Double(p1.getX(), p1.getY(), cp1.getX(), cp1.getY(), cp2.getX(), cp2.getY(),
                p2.getX(), p2.getY()
        );
    }

    public static CurveSplitResult splitCubicCurve(CubicCurve2D curve, double t) {
        Point2D a1 = lerp(curve.getP1(), curve.getCtrlP1(), t);
        Point2D a2 = lerp(curve.getCtrlP1(), curve.getCtrlP2(), t);
        Point2D a3 = lerp(curve.getCtrlP2(), curve.getP2(), t);

        Point2D b1 = lerp(a1, a2, t);
        Point2D b2 = lerp(a2, a3, t);

        Point2D c = lerp(b1, b2, t);

        return new CurveSplitResult(buildCurve(curve.getP1(), a1, b1, c), buildCurve(c, b2, a3, curve.getP2()));
    }

    public static Point2D getPointOnCubicCurve(CubicCurve2D curve, double t) {
        Point2D a1 = lerp(curve.getP1(), curve.getCtrlP1(), t);
        Point2D a2 = lerp(curve.getCtrlP1(), curve.getCtrlP2(), t);
        Point2D a3 = lerp(curve.getCtrlP2(), curve.getP2(), t);

        Point2D b1 = lerp(a1, a2, t);
        Point2D b2 = lerp(a2, a3, t);

        return lerp(b1, b2, t);
    }

    public static Point2D getDerivativeOfCubicCurve(CubicCurve2D curve, double t) {

        Point2D a1 = subtract(curve.getCtrlP1(), curve.getP1());
        Point2D a2 = subtract(curve.getCtrlP2(), curve.getCtrlP1());
        Point2D a3 = subtract(curve.getP2(), curve.getCtrlP2());

        Point2D b1 = lerp(a1, a2, t);
        Point2D b2 = lerp(a2, a3, t);

        return multiply(lerp(b1, b2, t), 3.0);
    }

    public static Point2D getSecondDerivativeOfCubicCurve(CubicCurve2D curve, double t) {
        Point2D a1 = subtract(curve.getCtrlP1(), curve.getP1());
        Point2D a2 = subtract(curve.getCtrlP2(), curve.getCtrlP1());
        Point2D a3 = subtract(curve.getP2(), curve.getCtrlP2());

        Point2D b1 = subtract(a2, a1);
        Point2D b2 = subtract(a3, a2);

        return multiply(lerp(b1, b2, t), 9.0);
    }

    public static AffineTransform optimisticInverse(AffineTransform transform) {
        try {
            return transform.createInverse();
        } catch (NoninvertibleTransformException ex) {
            throw new RuntimeException("Matrix inverse failed! Pessimists win :(");
        }
    }

    public static double getBorderPointParameter(Touchable collisionNode, ParametricCurve curve, double tStart, double tEnd) {
        Point2D point = new Point2D.Double();
        while (Math.abs(tEnd - tStart) > 1e-6) {
            double t = (tStart + tEnd) * 0.5;
            point = curve.getPointOnCurve(t);

            if (collisionNode.hitTest(point)) {
                tStart = t;
            } else {
                tEnd = t;
            }
        }
        return tStart;
    }

    public static PartialCurveInfo buildConnectionCurveInfo(VisualConnectionProperties connectionInfo, ParametricCurve curve, double endCutoff) {
        PartialCurveInfo info = new PartialCurveInfo();
        info.tStart = getBorderPointParameter(connectionInfo.getFirstShape(), curve, 0, 1);
        info.tEnd = getBorderPointParameter(connectionInfo.getSecondShape(), curve, 1, endCutoff);
        info.headPosition = curve.getPointOnCurve(info.tEnd);

        double dt = info.tEnd;
        double t = 0.0;
        double arrowLengthSq = connectionInfo.getArrowLength() * connectionInfo.getArrowLength();

        Point2D pt = new Point2D.Double();
        while (dt > 1e-6) {
            dt /= 2.0;
            t += dt;
            pt = curve.getPointOnCurve(t);
            if (info.headPosition.distanceSq(pt) < arrowLengthSq) {
                t -= dt;
            }
        }
        info.tEnd = t;
        info.headOrientation = Math.atan2(info.headPosition.getY() - pt.getY(), info.headPosition.getX() - pt.getX());
        return info;
    }

    public static Point2D changeBasis(Point2D p, Point2D vx, Point2D vy) {
        Point2D result = (Point2D) p.clone();

        if (dotProduct(vx, vy) > 0.0000001) {
            throw new RuntimeException("Vectors vx and vy must be orthogonal");
        }

        double vysq = vy.distanceSq(0, 0);
        double vxsq = vx.distanceSq(0, 0);

        if (vysq < 0.0000001 || vxsq < 0.0000001) {
            throw new RuntimeException("Vectors vx and vy must not have zero length");
        }

        result.setLocation(dotProduct(p, vx) / vxsq, dotProduct(p, vy) / vysq);

        return result;
    }

    public static double crossProduct(Point2D p, Point2D q) {
        double x1 = p.getX();
        double y1 = p.getY();

        double x2 = q.getX();
        double y2 = q.getY();

        return x1 * y2 - y1 * x2;
    }

    public static Rectangle2D getBoundingBoxOfCubicCurve(CubicCurve2D curve) {
        Path2D path = new Path2D.Double();
        path.moveTo(curve.getX1(), curve.getY1());
        // Cubic Bezier curve:
        // B(t) = (1 - t)^3 * p0 + 3 * (1 - t)^2 * t * p1 + 3 * (1 - t) * t^2 * p2 + t^3 * p3
        // Derivative:
        // dx/dt =  3 * (p1.x - p0.x) * (1-t)^2 + 6 * (p2.x - p1.x) * (1-t) * t + 3 * (p3.x - p2.x) * t^2
        //       =  (3 * p3.x - 9 * p2.x + 9 * p1.x - 3 * p0.x) * t^2 + (6 * p0.x - 12 * p1.x + 6 * p2.x) * t + 3 * (p1.x - p0.x)
        for (Double t : getBezierRoots(curve.getP1(), curve.getCtrlP1(), curve.getCtrlP2(), curve.getP2())) {
            if ((t > 0.0) && (t < 1.0)) {
                Point2D p = getPointOnCubicCurve(curve, t);
                path.lineTo(p.getX(), p.getY());
            }

        }
        path.lineTo(curve.getX2(), curve.getY2());
        return path.getBounds2D();
    }

    private static Set<Double> getBezierRoots(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
        Set<Double> result = new HashSet<>();

        double aX = 3.0 * p3.getX() - 9.0 * p2.getX() + 9.0 * p1.getX() - 3.0 * p0.getX();
        double bX = 6.0 * p0.getX() - 12.0 * p1.getX() + 6.0 * p2.getX();
        double cX = 3.0 * (p1.getX() - p0.getX());
        result.addAll(getQuadraticRoots(aX, bX, cX));

        double aY = 3.0 * p3.getY() - 9.0 * p2.getY() + 9.0 * p1.getY() - 3.0 * p0.getY();
        double bY = 6.0 * p0.getY() - 12.0 * p1.getY() + 6.0 * p2.getY();
        double cY = 3.0 * (p1.getY() - p0.getY());
        result.addAll(getQuadraticRoots(aY, bY, cY));
        return result;
    }

    private static Set<Double> getQuadraticRoots(double a, double b, double c) {
        Set<Double> result = new HashSet<>();
        double d2 = b * b - 4.0 * a * c;
        if (d2 >= 0.0) {
            double d = Math.sqrt(d2);
            result.add(0.5 * (-b + d) / a);
            result.add(0.5 * (-b - d) / a);
        }
        return result;
    }

}
