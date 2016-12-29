package org.workcraft.plugins.petri;

import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.Test;

public class VisualPlaceTests {

    @Test
    public void testHitTest() {
        Place p = new Place();
        VisualPlace vp = new VisualPlace(p);

        Assert.assertTrue(vp.hitTest(new Point2D.Double(0, 0)));
        Assert.assertFalse(vp.hitTest(new Point2D.Double(5, 5)));

        vp.setX(5);
        vp.setY(5);

        Assert.assertTrue(vp.hitTest(new Point2D.Double(5, 5)));
        Assert.assertFalse(vp.hitTest(new Point2D.Double(0, 0)));
    }

}
