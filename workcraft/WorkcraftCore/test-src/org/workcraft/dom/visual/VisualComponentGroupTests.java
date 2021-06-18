package org.workcraft.dom.visual;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;
import org.workcraft.observation.TransformEvent;
import org.workcraft.observation.TransformObserver;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class VisualComponentGroupTests {
    class MockTransformObservingNode implements Node, TransformObserver {
        private Node parent = null;

        public boolean notified = false;

        @Override
        public Collection<Node> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public Node getParent() {
            return parent;
        }

        @Override
        public void setParent(Node parent) {
            this.parent = parent;
        }

        @Override
        public void notify(TransformEvent e) {
            notified = true;
        }

        @Override
        public void subscribe(TransformDispatcher dispatcher) {
            dispatcher.subscribe(this, parent);
        }
    }

    @Test
    void testHitComponent() {
        VisualGroup root = createGroup(null);
        VisualGroup node1 = createGroup(root);
        node1.setX(5);

        SquareNode sq1 = new SquareNode(new Rectangle2D.Double(1, 1, 1, 1));
        SquareNode sq2 = new SquareNode(new Rectangle2D.Double(2, 2, 1, 1));
        root.add(sq1);
        node1.add(sq2);

        Assertions.assertEquals(sq1, HitMan.hitDeepest(new Point2D.Double(1.5, 1.5), root, VisualComponent.class));
        Assertions.assertEquals(sq2, HitMan.hitDeepest(new Point2D.Double(7.5, 2.5), root, VisualComponent.class));
        Assertions.assertEquals(null, HitMan.hitDeepest(new Point2D.Double(2.5, 2.5), root, VisualComponent.class));
    }

    @Disabled
    @Test
    void testHitConnection() {
        VisualGroup root = createGroup(null);
        VisualGroup group = createGroup(root);
        group.setX(5);

        SquareNode sqr1 = new SquareNode(new Rectangle2D.Double(1, 1, 1, 1));
        SquareNode sqr2 = new SquareNode(new Rectangle2D.Double(3, 3, 1, 1));
        root.add(sqr1);
        root.add(sqr2);
        VisualConnectionProperties connectionR = Tools.createConnection(sqr1, sqr2, root);

        SquareNode sqg1 = new SquareNode(new Rectangle2D.Double(1, 1, 1, 1));
        SquareNode sqg2 = new SquareNode(new Rectangle2D.Double(3, 3, 1, 1));
        group.add(sqg1);
        group.add(sqg2);
        Tools.createConnection(sqg1, sqg2, group);

        Assertions.assertEquals(connectionR, HitMan.hitFirstChild(new Point2D.Double(2.5, 1.5), root));
        Assertions.assertEquals(group, HitMan.hitFirstChild(new Point2D.Double(7.5, 1.5), root));
    }

    @Test
    void testHitNode() {
        VisualGroup group = new VisualGroup();

        Rectangle2D.Double r1 = new Rectangle2D.Double();
        Rectangle2D.Double r1b = new Rectangle2D.Double();
        Rectangle2D.Double r2 = new Rectangle2D.Double();
        Rectangle2D.Double r2b = new Rectangle2D.Double();
        Rectangle2D.Double r3 = new Rectangle2D.Double();
        Rectangle2D.Double r3b = new Rectangle2D.Double();

        r1.setRect(0, 0, 2, 2);
        r1b.setRect(0.1, 0.1, 1.8, 1.8);
        r2.setRect(0.5, 0.5, 2, 2);
        r2b.setRect(0.6, 0.6, 1.8, 1.8);
        r3.setRect(1, 1, 2, 2);
        r3b.setRect(1.1, 1.1, 1.8, 1.8);

        VisualNode node1 = new SquareNode(r1, r1b);
        VisualNode node2 = new SquareNode(r2, r2b);
        VisualNode node3 = new SquareNode(r3, r3b);

    //    Assertions.assertNull(group.getBoundingBoxInLocalSpace());

        group.add(node1);
        group.add(node2);
        group.add(node3);
        Assertions.assertNull(HitMan.hitFirstChild(new Point2D.Double(-1, -1), group));
        Assertions.assertNull(HitMan.hitFirstChild(new Point2D.Double(10, 10), group));
        Assertions.assertNull(HitMan.hitFirstChild(new Point2D.Double(0.05, 0.05), group));
        Assertions.assertEquals(node1, HitMan.hitFirstChild(new Point2D.Double(0.15, 0.5), group));
        Assertions.assertEquals(node1, HitMan.hitFirstChild(new Point2D.Double(0.55, 0.55), group));
        Assertions.assertEquals(node2, HitMan.hitFirstChild(new Point2D.Double(0.65, 0.65), group));
        Assertions.assertEquals(node2, HitMan.hitFirstChild(new Point2D.Double(1.05, 1.05), group));
        Assertions.assertEquals(node3, HitMan.hitFirstChild(new Point2D.Double(1.15, 1.15), group));
        Assertions.assertEquals(node3, HitMan.hitFirstChild(new Point2D.Double(1.95, 1.95), group));
        Assertions.assertEquals(node3, HitMan.hitFirstChild(new Point2D.Double(2.35, 1.35), group));
        Assertions.assertEquals(node3, HitMan.hitFirstChild(new Point2D.Double(2.45, 1.45), group));
        Assertions.assertEquals(node3, HitMan.hitFirstChild(new Point2D.Double(2.85, 2.85), group));
        Assertions.assertNull(HitMan.hitFirstChild(new Point2D.Double(2.95, 2.95), group));
    }

    @Test
    void testHitSubGroup() {
        VisualGroup root = new VisualGroup();

        VisualGroup node1 = new VisualGroup();
        VisualGroup node2 = new VisualGroup();
        root.add(node1);
        root.add(node2);
        node1.add(new SquareNode(new Rectangle2D.Double(0, 0, 1, 1)));
        node2.add(new SquareNode(new Rectangle2D.Double(1, 1, 1, 1)));
        Assertions.assertEquals(node2, HitMan.hitFirstChild(new Point2D.Double(1.5, 1.5), root));
        Assertions.assertEquals(node1, HitMan.hitFirstChild(new Point2D.Double(0.5, 0.5), root));
    }

    @Test
    void testUngroup() {
        VisualGroup root = new VisualGroup();

        VisualGroup node1 = new VisualGroup();
        root.add(node1);

        node1.setX(10);
        node1.setY(15);

        VisualGroup node2 = new VisualGroup();
        node1.add(node2);

        SquareNode sq1 = new SquareNode(new Rectangle2D.Double(0, 0, 1, 1));
        node1.add(sq1);
        SquareNode sq2 = new SquareNode(new Rectangle2D.Double(1, 1, 1, 1));
        node1.add(sq2);
        SquareNode sq3 = new SquareNode(new Rectangle2D.Double(2, 2, 1, 1));
        node1.add(sq3);

        Assertions.assertEquals(sq1, HitMan.hitFirstChild(new Point2D.Double(10.5, 15.5), node1));
        Assertions.assertEquals(sq2, HitMan.hitFirstChild(new Point2D.Double(11.5, 16.5), node1));

        Assertions.assertEquals(node1, HitMan.hitFirstChild(new Point2D.Double(10.5, 15.5), root));
        Assertions.assertEquals(node1, HitMan.hitFirstChild(new Point2D.Double(11.5, 16.5), root));
        Assertions.assertEquals(null, HitMan.hitFirstChild(new Point2D.Double(13.5, 15.5), root));

        Iterable<VisualNode> unGroup = node1.unGroup();
        ArrayList<Node> list = new ArrayList<>();
        for (Node node: unGroup) {
            list.add(node);
        }

        Assertions.assertEquals(4, list.size());
        Assertions.assertTrue(list.contains(sq1));
        Assertions.assertTrue(list.contains(sq2));
        Assertions.assertTrue(list.contains(sq3));
        Assertions.assertTrue(list.contains(node2));

        Assertions.assertTrue(list.indexOf(sq2) > list.indexOf(sq1));
        Assertions.assertTrue(list.indexOf(sq3) > list.indexOf(sq2));

        Assertions.assertNull(HitMan.hitFirstChild(new Point2D.Double(0.5, 0.5), node1));
        Assertions.assertNull(HitMan.hitFirstChild(new Point2D.Double(1.5, 1.5), node1));
    }

    private VisualGroup createGroup(VisualGroup parent) {
        return VisualNodeTests.createGroup(parent);
    }

    @Test
    void testTransformChangeNotification() {
        TransformEventPropagator p = new TransformEventPropagator();

        VisualGroup root = createGroup(null);

        MockTransformObservingNode node1 = new MockTransformObservingNode();
        root.add(node1);

        p.attach(root);

        Assertions.assertFalse(node1.notified, "already hit o_O");
        root.setX(8);
        Assertions.assertTrue(node1.notified, "not hit");
    }

    class DummyNode extends VisualComponent {
        DummyNode(VisualGroup parent) {
            super(null);
            parent.add(this);
        }

        @Override
        public Rectangle2D getBoundingBoxInLocalSpace() {
            return new Rectangle2D.Double(0, 0, 1, 1);
        }

        @Override
        public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
            return false;
        }

        @Override
        public Collection<MathNode> getMathReferences() {
            return null;
        }
    }

}
