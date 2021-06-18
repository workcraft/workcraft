package org.workcraft.plugins.petri;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VisualPlaceTests {

    @Test
    void testHitTest() {
        Place p = new Place();
        VisualPlace vp = new VisualPlace(p);

        Assertions.assertTrue(vp.hitTest(new Point2D.Double(0, 0)));
        Assertions.assertFalse(vp.hitTest(new Point2D.Double(5, 5)));

        vp.setX(5);
        vp.setY(5);

        Assertions.assertTrue(vp.hitTest(new Point2D.Double(5, 5)));
        Assertions.assertFalse(vp.hitTest(new Point2D.Double(0, 0)));
    }

}
