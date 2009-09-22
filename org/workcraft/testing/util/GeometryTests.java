package org.workcraft.testing.util;

import java.awt.geom.CubicCurve2D;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.util.Geometry;
import org.workcraft.util.Geometry.CurveSplitResult;

public class GeometryTests {
	@Test
	public void EmptyCurveTest()
	{
		Geometry.splitCubicCurve(new CubicCurve2D.Double(), 0.5);
	}

	private CubicCurve2D.Double getSimpleCurve()
	{
		return new CubicCurve2D.Double(0, 0, 0, 1, 1, 1, 1, 0);
	}

//	@Test
//	public void BordersTest1()
//	{
//		CubicCurve2D curve = getSimpleCurve();
//		CurveSplitResult split = Geometry.splitCubicCurve(curve, 0);
//		Assert.assertEquals(0.0, split.splitPoint.getX(), 1e-8);
//		Assert.assertEquals(0.0, split.splitPoint.getY(), 1e-8);
//		Assert.assertEquals(0.0, split.control1.getX(), 1e-8);
//		Assert.assertEquals(0.0, split.control1.getY(), 1e-8);
//		Assert.assertEquals(0.0, split.control2.getX(), 1e-8);
//		Assert.assertEquals(1.0, split.control2.getY(), 1e-8);
//	}
//
//	@Test
//	public void BordersTest2()
//	{
//		CubicCurve2D curve = getSimpleCurve();
//		CurveSplitResult split = Geometry.splitCubicCurve(curve, 1);
//		Assert.assertEquals(1.0, split.splitPoint.getX(), 1e-8);
//		Assert.assertEquals(0.0, split.splitPoint.getY(), 1e-8);
//		Assert.assertEquals(1.0, split.control1.getX(), 1e-8);
//		Assert.assertEquals(1.0, split.control1.getY(), 1e-8);
//		Assert.assertEquals(1.0, split.control2.getX(), 1e-8);
//		Assert.assertEquals(0.0, split.control2.getY(), 1e-8);
//	}
//
//	@Test
//	public void CenterTest()
//	{
//		CubicCurve2D curve = getSimpleCurve();
//		CurveSplitResult split = Geometry.splitCubicCurve(curve, 0.5);
//		Assert.assertEquals(0.5, split.splitPoint.getX(), 1e-8);
//		Assert.assertEquals(0.75, split.splitPoint.getY(), 1e-8);
//		Assert.assertEquals(0.25, split.control1.getX(), 1e-8);
//		Assert.assertEquals(0.75, split.control1.getY(), 1e-8);
//		Assert.assertEquals(0.75, split.control2.getX(), 1e-8);
//		Assert.assertEquals(0.75, split.control2.getY(), 1e-8);
//	}
//
//
//	@Test
//	public void CenterTest2()
//	{
//		CubicCurve2D curve = new CubicCurve2D.Double(-1, -2, -1, -1, 1, 1, 1, 2);
//		CurveSplitResult split = Geometry.splitCubicCurve(curve, 0.5);
//		Assert.assertEquals(0.0, split.splitPoint.getX(), 1e-8);
//		Assert.assertEquals(0.0, split.splitPoint.getY(), 1e-8);
//		Assert.assertEquals(-0.5, split.control1.getX(), 1e-8);
//		Assert.assertEquals(-0.75, split.control1.getY(), 1e-8);
//		Assert.assertEquals(0.5, split.control2.getX(), 1e-8);
//		Assert.assertEquals(0.75, split.control2.getY(), 1e-8);
//	}
//
//	@Test
//	public void OneThirdTest()
//	{
//		CubicCurve2D curve = new CubicCurve2D.Double(0, 0, 1, 1, 2, 2, 3, 3);
//		CurveSplitResult split = Geometry.splitCubicCurve(curve, 0.3);
//		Assert.assertEquals(0.9, split.splitPoint.getX(), 1e-8);
//		Assert.assertEquals(0.9, split.splitPoint.getY(), 1e-8);
//		Assert.assertEquals(0.6, split.control1.getX(), 1e-8);
//		Assert.assertEquals(0.6, split.control1.getY(), 1e-8);
//		Assert.assertEquals(1.6, split.control2.getX(), 1e-8);
//		Assert.assertEquals(1.6, split.control2.getY(), 1e-8);
//	}

}
