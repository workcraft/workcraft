/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.ParametricCurve;
import org.workcraft.dom.visual.connections.PartialCurveInfo;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;

public class Geometry {

    public static Point2D lerp(Point2D p1, Point2D p2, double t) {
        return new Point2D.Double(p1.getX()*(1-t)+p2.getX()*t, p1.getY()*(1-t)+p2.getY()*t);
    }
    public static Point2D middle(Point2D p1, Point2D p2) {
        return lerp(p1, p2, 0.5);
    }

    public static Point2D add(Point2D p1, Point2D p2) {
        Point2D result = (Point2D)p1.clone();
        result.setLocation(result.getX() + p2.getX(), result.getY() + p2.getY());
        return result;
    }

    public static Point2D subtract(Point2D p1, Point2D p2) {
        Point2D result = (Point2D)p1.clone();
        result.setLocation(result.getX() - p2.getX(), result.getY() - p2.getY());
        return result;
    }

    public static Point2D rotate90CCW(Point2D p) {
        Point2D result = (Point2D)p.clone();
        result.setLocation(-p.getY(), p.getX());
        return result;
    }

    public static Point2D normalize(Point2D p) {
        Point2D result = (Point2D)p.clone();
        double length = p.distance(0, 0);
        if (length < 0.0000001)
            result.setLocation(0, 0);
        else
            result.setLocation(p.getX() / length, p.getY() / length);
        return result;
    }

    public static Point2D reduce(Point2D p) {
        Point2D result = multiply(normalize(p), Math.pow(p.distanceSq(0, 0), 0.2));
        return result;
    }

    public static double dotProduct(Point2D v1, Point2D v2) {
        return v1.getX() * v2.getX() + v1.getY() * v2.getY();
    }

    public static Point2D multiply(Point2D p, double a) {
        Point2D result = (Point2D)p.clone();
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
        } catch(NoninvertibleTransformException ex) {
            throw new RuntimeException("Matrix inverse failed! Pessimists win :(");
        }
    }


    public static double getBorderPointParameter(Touchable collisionNode, ParametricCurve curve, double tStart, double tEnd) {
        Point2D point = new Point2D.Double();
        while(Math.abs(tEnd-tStart) > 1e-6) {
            double t = (tStart + tEnd)*0.5;
            point = curve.getPointOnCurve(t);

            if (collisionNode.hitTest(point))
                tStart = t;
            else
                tEnd = t;
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
        while(dt > 1e-6) {
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
        Point2D result = (Point2D)p.clone();

        if (dotProduct(vx,vy) > 0.0000001)
            throw new RuntimeException("Vectors vx and vy must be orthogonal");

        double vysq = vy.distanceSq(0,0);
        double vxsq = vx.distanceSq(0,0);

        if (vysq < 0.0000001 || vxsq < 0.0000001)
            throw new RuntimeException("Vectors vx and vy must not have zero length");

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

}
