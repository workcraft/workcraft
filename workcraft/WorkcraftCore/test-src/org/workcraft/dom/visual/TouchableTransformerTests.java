package org.workcraft.dom.visual;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class TouchableTransformerTests {

    private static class Dummy implements Touchable {
        @Override
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
    void testHitTestIdentity() {
        testHitTestIdentity(new Point2D.Double(88, 33), true);
        testHitTestIdentity(new Point2D.Double(0, 0), false);
        testHitTestIdentity(new Point2D.Double(-8, 3), false);
        testHitTestIdentity(new Point2D.Double(0, 0), true);
    }

    private void testHitTestIdentity(final Point2D point, final boolean result) {
        Dummy dummy = new Dummy() {
            @Override
            public boolean hitTest(Point2D p) {
                Assertions.assertEquals(point, p);
                return result;
            }
        };

        TouchableTransformer transformer = new TouchableTransformer(dummy, new AffineTransform());

        Assertions.assertEquals(result, transformer.hitTest(point));
    }

    @Test
    void testBBIdentity() {
        testBBIdentity(new Rectangle2D.Double(88, 33, 3, 3));
        testBBIdentity(new Rectangle2D.Double(0, 0, 10, 10));
        testBBIdentity(null);
        testBBIdentity(new Rectangle2D.Double(-5, -5, 8, 0));
    }

    private void testBBIdentity(final Rectangle2D bb) {
        Dummy dummy = new Dummy() {
            @Override
            public Rectangle2D getBoundingBox() {
                if (bb == null) {
                    return null;
                }
                return new Rectangle2D.Double(bb.getMinX(), bb.getMinY(), bb.getWidth(), bb.getHeight());
            }
        };

        Assertions.assertEquals(bb,
                new TouchableTransformer(dummy, new AffineTransform()).getBoundingBox());
    }

    @Test
    void testRotateBoundingBox() {
        final Rectangle2D bb = new Rectangle2D.Double(0, 0, 1, 1);
        AffineTransform transform = AffineTransform.getRotateInstance(3.1415926535897932384626433832795 / 4.0);

        Rectangle2D result = new TouchableTransformer(
                new Dummy() {
                    @Override
                    public Rectangle2D getBoundingBox() {
                        return bb;
                    }
                },
                transform).getBoundingBox();

        double sqrt2 = Math.sqrt(2.0);
        double sqrt2by2 = sqrt2 / 2.0;

        Assertions.assertEquals(sqrt2, result.getMaxY(), 1e-5);
        Assertions.assertEquals(0, result.getMinY(), 1e-5);
        Assertions.assertEquals(sqrt2by2, result.getMaxX(), 1e-5);
        Assertions.assertEquals(-sqrt2by2, result.getMinX(), 1e-5);
    }

    @Test
    void testTranslateHitTest() {
        TouchableTransformer toucher = new TouchableTransformer(
                new Dummy() {
                    @Override
                    public boolean hitTest(Point2D point) {
                        return point.distanceSq(0, 0) < 1.0;
                    }
                }, AffineTransform.getTranslateInstance(10, 1));

        Assertions.assertTrue(toucher.hitTest(new Point2D.Double(10, 1)));
        Assertions.assertTrue(toucher.hitTest(new Point2D.Double(10.9, 1)));
        Assertions.assertTrue(toucher.hitTest(new Point2D.Double(9.1, 1)));
        Assertions.assertFalse(toucher.hitTest(new Point2D.Double(11.1, 1)));
        Assertions.assertFalse(toucher.hitTest(new Point2D.Double(8.9, 1)));
        Assertions.assertTrue(toucher.hitTest(new Point2D.Double(10.6, 1.6)));
        Assertions.assertFalse(toucher.hitTest(new Point2D.Double(10.8, 1.8)));
        Assertions.assertTrue(toucher.hitTest(new Point2D.Double(9.4, 0.4)));
        Assertions.assertFalse(toucher.hitTest(new Point2D.Double(9.2, 0.2)));
    }

}
