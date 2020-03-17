package org.workcraft.dom.visual;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class VisualNodeTests {

    public static VisualGroup createGroup(VisualGroup parent) {
        return Tools.createGroup(parent);
    }

    @Test
    public void testTransformChangeNotify() {
        final SquareNode node = new SquareNode(null, new Rectangle2D.Double(0, 0, 1, 1));
        final Boolean[] hit = {false};
        node.addObserver(new StateObserver() {
            @Override
            public void notify(StateEvent e) {
                if (e instanceof TransformChangedEvent) {
                    if (e.getSender() == node) {
                        hit[0] = true;
                    }

                }
            }
        });
        Assert.assertFalse("already hit o_O", hit[0]);
        node.setX(8);
        Assert.assertTrue("not hit", hit[0]);
    }

    @Test
    public void testParentToAncestorTransform() {
        VisualGroup root = createGroup(null);
        VisualGroup node1 = createGroup(root);
        VisualGroup node2 = createGroup(root);
        VisualGroup node11 = createGroup(node1);
        VisualGroup node111 = createGroup(node11);
        VisualGroup node1111 = createGroup(node111);

        node1.setX(1);
        node1.setX(1);
        node11.setX(10);
        node111.setX(100);
        node1111.setX(1000);
        node2.setX(5);

        ensureShiftX(node1111, root, 111);
        ensureShiftX(node111, root, 11);
        ensureShiftX(node11, root, 1);
        ensureShiftX(node1, root, 0);

        ensureShiftX(node1111, node1, 111);
        ensureShiftX(node111, node1, 11);
        ensureShiftX(node11, node1, 1);

        ensureShiftX(node1111, node11, 110);
        ensureShiftX(node111, node11, 10);
        ensureShiftX(node1, node11, -1);

        ensureShiftX(node1111, node111, 100);
        ensureShiftX(node11, node111, -10);
        ensureShiftX(node1, node111, -11);

        ensureShiftX(node111, node1111, -100);
        ensureShiftX(node11, node1111, -110);
        ensureShiftX(node1, node1111, -111);

        ensureShiftX(node2, root, 0);
        ensureShiftX(node2, node1, 0);
        ensureShiftX(node2, node11, -1);
        ensureShiftX(node2, node111, -11);
        ensureShiftX(node2, node1111, -111);

        ensureShiftX(node1, node2, 0);
        ensureShiftX(node11, node2, 1);
        ensureShiftX(node111, node2, 11);
        ensureShiftX(node1111, node2, 111);
    }

    @Test
    public void testGetPath() {
        VisualGroup root = createGroup(null);
        Assert.assertEquals(1, Hierarchy.getPath(root).length);
        VisualGroup node1 = createGroup(root);
        Assert.assertArrayEquals(new VisualGroup[]{root, node1}, Hierarchy.getPath(node1));
        VisualGroup node2 = createGroup(node1);
        Assert.assertArrayEquals(new VisualGroup[]{root, node1, node2}, Hierarchy.getPath(node2));
    }

    @Test
    public void testFindCommonParent() {
        VisualGroup root = createGroup(null);
        VisualGroup node1 = createGroup(root);
        VisualGroup node2 = createGroup(root);
        VisualGroup node11 = createGroup(node1);
        VisualGroup node12 = createGroup(node1);
        VisualGroup node21 = createGroup(node2);
        VisualGroup node22 = createGroup(node2);

        Assert.assertEquals(root, Hierarchy.getCommonParent(node1, node2));
        Assert.assertEquals(root, Hierarchy.getCommonParent(node1, node21));
        Assert.assertEquals(root, Hierarchy.getCommonParent(node1, node22));

        Assert.assertEquals(root, Hierarchy.getCommonParent(node11, node2));
        Assert.assertEquals(root, Hierarchy.getCommonParent(node11, node21));
        Assert.assertEquals(root, Hierarchy.getCommonParent(node11, node22));

        Assert.assertEquals(root, Hierarchy.getCommonParent(node12, node2));
        Assert.assertEquals(root, Hierarchy.getCommonParent(node12, node21));
        Assert.assertEquals(root, Hierarchy.getCommonParent(node12, node22));

        Assert.assertEquals(node1, Hierarchy.getCommonParent(node11, node1));

        Assert.assertEquals(node1, Hierarchy.getCommonParent(node11, node12));
        Assert.assertEquals(node11, Hierarchy.getCommonParent(node11, node11));

        Assert.assertEquals(node1, Hierarchy.getCommonParent(node12, node11));
        Assert.assertEquals(node12, Hierarchy.getCommonParent(node12, node12));
    }

    private void ensureShiftX(VisualGroup node, VisualGroup ancestor, double i) {
        ensureShiftX(TransformHelper.getTransform(node, ancestor), i);
        ensureShiftX(TransformHelper.getTransform(ancestor, node), -i);
    }

    private void ensureShiftX(AffineTransform transform, double i) {
        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        asserArrayEquals(new double[]{1, 0, 0, 1, i, 0}, matrix);
    }

    private void asserArrayEquals(double[] expected, double[] actual) {
        Assert.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertClose(expected[i], actual[i]);
        }
    }

    private void assertClose(double expected, double actual) {
        double eps = 1e-6;
        Assert.assertTrue("Expected: " + expected + ", actual: " + actual, expected - eps <= actual && expected + eps >= actual);
    }

}
