package org.workcraft.testing.plugins.balsa;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.Test;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.VisualBreezeComponent;
import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.components.While;


public class VisualTests {
	@Test
	public void whileHitTest()
	{
		BalsaCircuit circuit = new BalsaCircuit();
		BreezeComponent mathComp = new BreezeComponent();
		mathComp.setUnderlyingComponent(new While());
		circuit.addComponent(mathComp);
		VisualBreezeComponent visual = new VisualBreezeComponent(mathComp);

		assertTrue(visual.hitTest(new Point2D.Double(0, 0)));
		assertTrue(visual.hitTest(new Point2D.Double(0.4, 0)));
		assertTrue(visual.hitTest(new Point2D.Double(0.3, 0.3)));
		assertFalse(visual.hitTest(new Point2D.Double(3, 0)));
	}

	@Test
	public void concurBoundingBoxTest()
	{
		BalsaCircuit circuit = new BalsaCircuit();
		BreezeComponent mathComp = new BreezeComponent();
		mathComp.setUnderlyingComponent(new Concur());
		circuit.addComponent(mathComp);
		VisualBreezeComponent visual = new VisualBreezeComponent(mathComp);

		Rectangle2D box = visual.getBoundingBoxInLocalSpace();

		assertTrue(-0.51 > box.getMinX());
		assertTrue(-0.8 < box.getMinX());

		assertTrue(0.51 < box.getMaxX());
		assertTrue(0.8 > box.getMaxX());

		assertTrue(0.49 < box.getMaxY());
		assertTrue(0.61 > box.getMaxY());

		assertTrue(-0.49 > box.getMinY());
		assertTrue(-0.61 < box.getMinY());
	}
}
