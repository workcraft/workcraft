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
