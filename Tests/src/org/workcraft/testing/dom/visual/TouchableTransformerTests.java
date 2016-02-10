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

package org.workcraft.testing.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static org.junit.Assert.*;
import org.junit.Test;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TouchableTransformer;

public class TouchableTransformerTests {

    private static class Dummy implements Touchable {
        public Rectangle2D getBoundingBox() {
            throw new RuntimeException("not implemented");
        }

        @Override
        public boolean hitTest(Point2D point) {
            throw new RuntimeException("not implemented");
        }

        @Override
        public Point2D getCenter() {
            throw new RuntimeException("not implemented");
        }
    }

    @Test
    public void TestHitTestIdentity() {
        testHitTestIdentity(new Point2D.Double(88, 33), true);
        testHitTestIdentity(new Point2D.Double(0, 0), false);
        testHitTestIdentity(new Point2D.Double(-8, 3), false);
        testHitTestIdentity(new Point2D.Double(0, 0), true);
    }

    private void testHitTestIdentity(final Point2D point, final boolean result) {
        Dummy dummy = new Dummy() {
            @Override
            public boolean hitTest(Point2D p) {
                assertEquals(point, p);
                return result;
            }
        };

        TouchableTransformer transformer =
            new TouchableTransformer(dummy, new AffineTransform());

        assertEquals(result, transformer.hitTest(point));
    }

    @Test
    public void TestBBIdentity() {
        testBBIdentity(new Rectangle2D.Double(88, 33, 3, 3));
        testBBIdentity(new Rectangle2D.Double(0, 0, 10, 10));
        testBBIdentity(null);
        testBBIdentity(new Rectangle2D.Double(-5, -5, 8, 0));
    }

    private void testBBIdentity(final Rectangle2D bb) {
        Dummy dummy = new Dummy() {
            @Override
            public Rectangle2D getBoundingBox() {
                if(bb == null)
                    return null;
                return new Rectangle2D.Double(bb.getMinX(), bb.getMinY(), bb.getWidth(), bb.getHeight());
            }
        };

        assertEquals(bb,
            new TouchableTransformer(dummy, new AffineTransform()).getBoundingBox());
    }

    @Test
    public void TestRotateBoundingBox() {
        final Rectangle2D bb = new Rectangle2D.Double(0, 0, 1, 1);
        AffineTransform transform = AffineTransform.getRotateInstance(3.1415926535897932384626433832795/4.0);

        Rectangle2D result = new TouchableTransformer(
                new Dummy(){
                    @Override
                    public Rectangle2D getBoundingBox() {
                        return bb;
                    }
        },
        transform).getBoundingBox();

        double sqrt2 = Math.sqrt(2.0);
        double sqrt2by2 = sqrt2 / 2.0;

        assertEquals(sqrt2, result.getMaxY(), 1e-5);
        assertEquals(0, result.getMinY(), 1e-5);
        assertEquals(sqrt2by2, result.getMaxX(), 1e-5);
        assertEquals(-sqrt2by2, result.getMinX(), 1e-5);
    }


    @Test
    public void TestTranslateHitTest() {
        TouchableTransformer toucher = new TouchableTransformer(
                new Dummy(){
                    @Override
                    public boolean hitTest(Point2D point) {
                        return point.distanceSq(0, 0) < 1.0;
                    }
        }, AffineTransform.getTranslateInstance(10, 1));

        assertTrue(toucher.hitTest(new Point2D.Double(10, 1)));
        assertTrue(toucher.hitTest(new Point2D.Double(10.9, 1)));
        assertTrue(toucher.hitTest(new Point2D.Double(9.1, 1)));
        assertFalse(toucher.hitTest(new Point2D.Double(11.1, 1)));
        assertFalse(toucher.hitTest(new Point2D.Double(8.9, 1)));
        assertTrue(toucher.hitTest(new Point2D.Double(10.6, 1.6)));
        assertFalse(toucher.hitTest(new Point2D.Double(10.8, 1.8)));
        assertTrue(toucher.hitTest(new Point2D.Double(9.4, 0.4)));
        assertFalse(toucher.hitTest(new Point2D.Double(9.2, 0.2)));
    }

}
