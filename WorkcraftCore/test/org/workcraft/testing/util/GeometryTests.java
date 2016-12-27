package org.workcraft.testing.util;

import java.awt.geom.CubicCurve2D;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.util.Geometry;
import org.workcraft.util.Geometry.CurveSplitResult;

public class GeometryTests {
    @Test
    public void emptyCurveTest() {
        Geometry.splitCubicCurve(new CubicCurve2D.Double(), 0.5);
    }

    private CubicCurve2D.Double getSimpleCurve() {
        return new CubicCurve2D.Double(0, 0, 0, 1, 1, 1, 1, 0);
    }

    @Test
    public void bordersTest1() {
        CubicCurve2D curve = getSimpleCurve();
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 0);
        Assert.assertEquals(0.0, split.curve1.getX2(), 1e-8);
        Assert.assertEquals(0.0, split.curve2.getY1(), 1e-8);
        Assert.assertEquals(0.0, split.curve1.getCtrlX2(), 1e-8);
        Assert.assertEquals(0.0, split.curve1.getCtrlY2(), 1e-8);
        Assert.assertEquals(0.0, split.curve2.getCtrlX1(), 1e-8);
        Assert.assertEquals(1.0, split.curve2.getCtrlY1(), 1e-8);
    }

    @Test
    public void bordersTest2() {
        CubicCurve2D curve = getSimpleCurve();
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 1);
        Assert.assertEquals(1.0, split.curve1.getX2(), 1e-8);
        Assert.assertEquals(0.0, split.curve2.getY1(), 1e-8);
        Assert.assertEquals(1.0, split.curve1.getCtrlX2(), 1e-8);
        Assert.assertEquals(1.0, split.curve1.getCtrlY2(), 1e-8);
        Assert.assertEquals(1.0, split.curve2.getCtrlX1(), 1e-8);
        Assert.assertEquals(0.0, split.curve2.getCtrlY1(), 1e-8);
    }

    @Test
    public void centerTest() {
        CubicCurve2D curve = getSimpleCurve();
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 0.5);
        Assert.assertEquals(0.5, split.curve1.getX2(), 1e-8);
        Assert.assertEquals(0.75, split.curve2.getY1(), 1e-8);
        Assert.assertEquals(0.25, split.curve1.getCtrlX2(), 1e-8);
        Assert.assertEquals(0.75, split.curve1.getCtrlY2(), 1e-8);
        Assert.assertEquals(0.75, split.curve2.getCtrlX1(), 1e-8);
        Assert.assertEquals(0.75, split.curve2.getCtrlY1(), 1e-8);
    }

    @Test
    public void centerTest2() {
        CubicCurve2D curve = new CubicCurve2D.Double(-1, -2, -1, -1, 1, 1, 1, 2);
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 0.5);
        Assert.assertEquals(0.0, split.curve1.getX2(), 1e-8);
        Assert.assertEquals(0.0, split.curve2.getY1(), 1e-8);
        Assert.assertEquals(-0.5, split.curve1.getCtrlX2(), 1e-8);
        Assert.assertEquals(-0.75, split.curve1.getCtrlY2(), 1e-8);
        Assert.assertEquals(0.5, split.curve2.getCtrlX1(), 1e-8);
        Assert.assertEquals(0.75, split.curve2.getCtrlY1(), 1e-8);
    }

    @Test
    public void oneThirdTest() {
        CubicCurve2D curve = new CubicCurve2D.Double(0, 0, 1, 1, 2, 2, 3, 3);
        CurveSplitResult split = Geometry.splitCubicCurve(curve, 0.3);
        Assert.assertEquals(0.9, split.curve1.getX2(), 1e-8);
        Assert.assertEquals(0.9, split.curve2.getY1(), 1e-8);
        Assert.assertEquals(0.6, split.curve1.getCtrlX2(), 1e-8);
        Assert.assertEquals(0.6, split.curve1.getCtrlY2(), 1e-8);
        Assert.assertEquals(1.6, split.curve2.getCtrlX1(), 1e-8);
        Assert.assertEquals(1.6, split.curve2.getCtrlY1(), 1e-8);
    }

}
