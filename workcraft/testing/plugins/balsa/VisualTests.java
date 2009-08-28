package org.workcraft.testing.plugins.balsa;

import java.awt.geom.Point2D;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.VisualBreezeComponent;
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

		Assert.assertTrue(visual.hitTest(new Point2D.Double(0, 0)));
		Assert.assertTrue(visual.hitTest(new Point2D.Double(0.4, 0)));
		Assert.assertTrue(visual.hitTest(new Point2D.Double(0.3, 0.3)));
		Assert.assertFalse(visual.hitTest(new Point2D.Double(3, 0)));
	}
}
