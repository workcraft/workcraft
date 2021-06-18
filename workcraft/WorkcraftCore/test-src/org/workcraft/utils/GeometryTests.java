package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.utils.Geometry.CurveSplitResult;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;

class GeometryTests {

    @Test
    void emptyCurveTest() {
        Geometry.splitCubicCurve(new CubicCurve2D.Double(), 0.5);
    }

    private CubicCurve2D.Double getSimpleCurve() {
        return new CubicCurve2D.Double(0, 0, 0, 1, 1, 1, 1, 0);
    }

    @Test
    void bordersTest1() {
        CubicCurve2D curve = getSimpleCurve();
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 0);
        Assertions.assertEquals(0.0, split.curve1.getX2(), 1e-8);
        Assertions.assertEquals(0.0, split.curve2.getY1(), 1e-8);
        Assertions.assertEquals(0.0, split.curve1.getCtrlX2(), 1e-8);
        Assertions.assertEquals(0.0, split.curve1.getCtrlY2(), 1e-8);
        Assertions.assertEquals(0.0, split.curve2.getCtrlX1(), 1e-8);
        Assertions.assertEquals(1.0, split.curve2.getCtrlY1(), 1e-8);
    }

    @Test
    void bordersTest2() {
        CubicCurve2D curve = getSimpleCurve();
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 1);
        Assertions.assertEquals(1.0, split.curve1.getX2(), 1e-8);
        Assertions.assertEquals(0.0, split.curve2.getY1(), 1e-8);
        Assertions.assertEquals(1.0, split.curve1.getCtrlX2(), 1e-8);
        Assertions.assertEquals(1.0, split.curve1.getCtrlY2(), 1e-8);
        Assertions.assertEquals(1.0, split.curve2.getCtrlX1(), 1e-8);
        Assertions.assertEquals(0.0, split.curve2.getCtrlY1(), 1e-8);
    }

    @Test
    void centerTest() {
        CubicCurve2D curve = getSimpleCurve();
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 0.5);
        Assertions.assertEquals(0.5, split.curve1.getX2(), 1e-8);
        Assertions.assertEquals(0.75, split.curve2.getY1(), 1e-8);
        Assertions.assertEquals(0.25, split.curve1.getCtrlX2(), 1e-8);
        Assertions.assertEquals(0.75, split.curve1.getCtrlY2(), 1e-8);
        Assertions.assertEquals(0.75, split.curve2.getCtrlX1(), 1e-8);
        Assertions.assertEquals(0.75, split.curve2.getCtrlY1(), 1e-8);
    }

    @Test
    void centerTest2() {
        CubicCurve2D curve = new CubicCurve2D.Double(-1, -2, -1, -1, 1, 1, 1, 2);
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 0.5);
        Assertions.assertEquals(0.0, split.curve1.getX2(), 1e-8);
        Assertions.assertEquals(0.0, split.curve2.getY1(), 1e-8);
        Assertions.assertEquals(-0.5, split.curve1.getCtrlX2(), 1e-8);
        Assertions.assertEquals(-0.75, split.curve1.getCtrlY2(), 1e-8);
        Assertions.assertEquals(0.5, split.curve2.getCtrlX1(), 1e-8);
        Assertions.assertEquals(0.75, split.curve2.getCtrlY1(), 1e-8);
    }

    @Test
    void oneThirdTest() {
        CubicCurve2D curve = new CubicCurve2D.Double(0, 0, 1, 1, 2, 2, 3, 3);
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 0.3);
        Assertions.assertEquals(0.9, split.curve1.getX2(), 1e-8);
        Assertions.assertEquals(0.9, split.curve2.getY1(), 1e-8);
        Assertions.assertEquals(0.6, split.curve1.getCtrlX2(), 1e-8);
        Assertions.assertEquals(0.6, split.curve1.getCtrlY2(), 1e-8);
        Assertions.assertEquals(1.6, split.curve2.getCtrlX1(), 1e-8);
        Assertions.assertEquals(1.6, split.curve2.getCtrlY1(), 1e-8);
    }

    @Test
    void segmentFrameIntersectionTest() {
        Rectangle2D frame = new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0);

        Line2D segmentInside = new Line2D.Double(-0.5, -0.5, 0.5, 0.5);
        Assertions.assertEquals(new HashSet<>(),
                Geometry.getSegmentFrameIntersections(segmentInside, frame));

        Line2D segmentOutside = new Line2D.Double(2.0, 2.0, 3.0, 3.0);
        Assertions.assertEquals(new HashSet<>(),
                Geometry.getSegmentFrameIntersections(segmentOutside, frame));

        Line2D segmentBorderTop = new Line2D.Double(-1.0, 1.0, 1.0, 1.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(
                new Point2D.Double(-1.0, 1.0),
                new Point2D.Double(1.0, 1.0))),
                Geometry.getSegmentFrameIntersections(segmentBorderTop, frame));

        Line2D segmentBorderBottom = new Line2D.Double(-1.0, -1.0, 1.0, -1.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(
                new Point2D.Double(-1.0, -1.0),
                new Point2D.Double(1.0, -1.0))),
                Geometry.getSegmentFrameIntersections(segmentBorderBottom, frame));

        Line2D segmentBorderLeft = new Line2D.Double(-1.0, -1.0, -1.0, 1.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(
                new Point2D.Double(-1.0, 1.0),
                new Point2D.Double(-1.0, -1.0))),
                Geometry.getSegmentFrameIntersections(segmentBorderLeft, frame));

        Line2D segmentBorderRight = new Line2D.Double(1.0, -1.0, 1.0, 1.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(
                new Point2D.Double(1.0, 1.0),
                new Point2D.Double(1.0, -1.0))),
                Geometry.getSegmentFrameIntersections(segmentBorderRight, frame));

        Line2D segmentCrossTop = new Line2D.Double(0.0, 0.0, 2.0, 4.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(new Point2D.Double(0.5, 1.0))),
                Geometry.getSegmentFrameIntersections(segmentCrossTop, frame));

        Line2D segmentCrossBottom = new Line2D.Double(0.0, 0.0, -2.0, -4.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(new Point2D.Double(-0.5, -1.0))),
                Geometry.getSegmentFrameIntersections(segmentCrossBottom, frame));

        Line2D segmentCrossLeft = new Line2D.Double(0.0, 0.0, -4.0, -2.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(new Point2D.Double(-1.0, -0.5))),
                Geometry.getSegmentFrameIntersections(segmentCrossLeft, frame));

        Line2D segmentCrossRight = new Line2D.Double(0.0, 0.0, 4.0, 2.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(new Point2D.Double(1.0, 0.5))),
                Geometry.getSegmentFrameIntersections(segmentCrossRight, frame));

        Line2D segmentIntersect = new Line2D.Double(-2.0, -4.0, 2.0, 4.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(
                new Point2D.Double(0.5, 1.0),
                new Point2D.Double(-0.5, -1.0))),
                Geometry.getSegmentFrameIntersections(segmentIntersect, frame));
    }

    @Test
    void derivativeTest() {
        CubicCurve2D curve = getSimpleCurve();

        Assertions.assertEquals(new Point2D.Double(0.0, 3.0),
                Geometry.getDerivativeOfCubicCurve(curve, 0.0));

        Assertions.assertEquals(new Point2D.Double(0.0, -3.0),
                Geometry.getDerivativeOfCubicCurve(curve, 1.0));

        Assertions.assertEquals(new Point2D.Double(1.5, 0.0),
                Geometry.getDerivativeOfCubicCurve(curve, 0.5));

        Assertions.assertEquals(new Point2D.Double(9.0, -9.0),
                Geometry.getSecondDerivativeOfCubicCurve(curve, 0.0));

        Assertions.assertEquals(new Point2D.Double(-9.0, -9.0),
                Geometry.getSecondDerivativeOfCubicCurve(curve, 1.0));

        Assertions.assertEquals(new Point2D.Double(0.0, -9.0),
                Geometry.getSecondDerivativeOfCubicCurve(curve, 0.5));
    }

    @Test
    void changeBaseTest() {
        Assertions.assertThrows(RuntimeException.class,
                () -> Geometry.changeBasis(new Point2D.Double(0.0, 0.0),
                        new Point2D.Double(0.0, 0.0), new Point2D.Double(0.0, 0.0)));

        Assertions.assertThrows(RuntimeException.class,
                () -> Geometry.changeBasis(new Point2D.Double(0.0, 0.0),
                        new Point2D.Double(1.0, 0.0), new Point2D.Double(1.0, 1.0)));

        Assertions.assertEquals(new Point2D.Double(0.0, 0.0),
                Geometry.changeBasis(new Point2D.Double(0.0, 0.0),
                        new Point2D.Double(1.0, 0.0), new Point2D.Double(0.0, 1.0)));
    }

    @Test
    void crossProductTest() {
        Assertions.assertEquals(0.0, Geometry.crossProduct(
                new Point2D.Double(0.0, 0.0), new Point2D.Double(0.0, 0.0)));

        Assertions.assertEquals(0.0, Geometry.crossProduct(
                new Point2D.Double(0.0, 0.0), new Point2D.Double(123.0, 456.0)));

        Assertions.assertEquals(1.0, Geometry.crossProduct(
                new Point2D.Double(1.0, 0.0), new Point2D.Double(0.0, 1.0)));
    }

    @Test
    void cubicCurveBoundingBoxTest() {
        CubicCurve2D curve1 = new CubicCurve2D.Double(-0.5, -0.5, -1.0, -1.0, 1.0, 1.0, 0.5, 0.5);
        Assertions.assertEquals(new Rectangle2D.Double(-0.5809475019311126, -0.5809475019311126, 1.161895003862225, 1.161895003862225),
                Geometry.getBoundingBoxOfCubicCurve(curve1));

        CubicCurve2D curve2 = new CubicCurve2D.Double(-2.0, -2.0, -1.0, -1.0, 1.0, 1.0, 2.0, 2.0);
        Assertions.assertEquals(new Rectangle2D.Double(-2.0, -2.0, 4.0, 4.0),
                Geometry.getBoundingBoxOfCubicCurve(curve2));

        CubicCurve2D curve3 = new CubicCurve2D.Double(-2.0, -2.0, -1.0, -3.0, 3.0, 1.0, 2.0, 2.0);
        Assertions.assertEquals(new Rectangle2D.Double(-2.0, -2.159149863609382, 4.159149863609382, 4.159149863609382),
                Geometry.getBoundingBoxOfCubicCurve(curve3));

        CubicCurve2D curve4 = new CubicCurve2D.Double(-1.5, 0.0, 2.0, -2.0, 2.0, -2.0, 0.0, 1.5);
        Assertions.assertEquals(new Rectangle2D.Double(-1.5, -1.3513414910685637, 2.8513414910685637, 2.8513414910685637),
                Geometry.getBoundingBoxOfCubicCurve(curve4));

        CubicCurve2D curve5 = new CubicCurve2D.Double(-2.0, -2.0, -1.0, -5.0, 5.0, 1.0, 2.0, 2.0);
        Assertions.assertEquals(new Rectangle2D.Double(-2.0, -2.8321930809952813, 4.832193080995281, 4.832193080995282),
                Geometry.getBoundingBoxOfCubicCurve(curve5));
    }

    @Test
    void cubicCurveFrameIntersectionTest() {
        Rectangle2D frame = new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0);

        CubicCurve2D curveInside = new CubicCurve2D.Double(-0.5, -0.5, -1.0, -1.0, 1.0, 1.0, 0.5, 0.5);
        Assertions.assertEquals(new HashSet<>(),
                Geometry.getCubicCurveFrameIntersections(curveInside, frame));

        CubicCurve2D curveCrossTop = new CubicCurve2D.Double(0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 0.0, 2.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(new Point2D.Double(0.75, 1.0))),
                Geometry.getCubicCurveFrameIntersections(curveCrossTop, frame));

        CubicCurve2D curveCrossBottom = new CubicCurve2D.Double(0.0, 0.0, 1.0, -1.0, 1.0, -1.0, 0.0, -2.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(new Point2D.Double(0.75, -1.0))),
                Geometry.getCubicCurveFrameIntersections(curveCrossBottom, frame));

        CubicCurve2D curveCrossLeft = new CubicCurve2D.Double(0.0, 0.0, -1.0, 1.0, -1.0, 1.0, -2.0, 0.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(new Point2D.Double(-1.0, 0.75))),
                Geometry.getCubicCurveFrameIntersections(curveCrossLeft, frame));

        CubicCurve2D curveCrossRight = new CubicCurve2D.Double(0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 2.0, 0.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(new Point2D.Double(1.0, 0.75))),
                Geometry.getCubicCurveFrameIntersections(curveCrossRight, frame));

        CubicCurve2D curveDiagonal = new CubicCurve2D.Double(-2.0, -2.0, -1.0, -1.0, 1.0, 1.0, 2.0, 2.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(
                new Point2D.Double(-1.0, -1.0),
                new Point2D.Double(1.0, 1.0))),
                Geometry.getCubicCurveFrameIntersections(curveDiagonal, frame));

        CubicCurve2D curveCrossTwice = new CubicCurve2D.Double(-2.0, -2.0, -1.0, -3.0, 3.0, 1.0, 2.0, 2.0);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(
                new Point2D.Double(1.0, -0.48906990285462276),
                new Point2D.Double(0.48906990285462304, -1.0))),
                Geometry.getCubicCurveFrameIntersections(curveCrossTwice, frame));

        CubicCurve2D curveCrossMultiple = new CubicCurve2D.Double(-1.5, 0.0, 2.0, -2.0, 2.0, -2.0, 0.0, 1.5);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(
                new Point2D.Double(0.285417652844922, 1.0),
                new Point2D.Double(0.29139978906973985, -1.0),
                new Point2D.Double(-1.0, -0.28541765284492315),
                new Point2D.Double(1.0, -0.2913997890697412))),
                Geometry.getCubicCurveFrameIntersections(curveCrossMultiple, frame));

        CubicCurve2D curveOutside = new CubicCurve2D.Double(-2.0, 2.0, -2.0, 5.0, 2.0, 5.0, 2.0, 2.0);
        Assertions.assertEquals(new HashSet<>(),
                Geometry.getCubicCurveFrameIntersections(curveOutside, frame));

        CubicCurve2D curveAround = new CubicCurve2D.Double(-2.0, -2.0, -1.0, -5.0, 5.0, 1.0, 2.0, 2.0);
        Assertions.assertEquals(new HashSet<>(),
                Geometry.getCubicCurveFrameIntersections(curveAround, frame));
    }

}
