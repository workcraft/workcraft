package org.workcraft.dom.visual;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.Node;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

class HitmanTests {

    class DummyNode implements Node {
        private final Collection<Node> children;

        DummyNode() {
            this(Collections.emptyList());
        }

        DummyNode(Node[] children) {
            this(Arrays.asList(children));
        }

        DummyNode(Collection<Node> children) {
            this.children = children;
        }

        @Override
        public Collection<Node> getChildren() {
            return children;
        }

        @Override
        public Node getParent() {
            throw new RuntimeException("Not Implemented");
        }

        @Override
        public void setParent(Node parent) {
            throw new RuntimeException("Not Implemented");
        }
    }

    class HitableNode extends DummyNode implements Touchable {

        @Override
        public Rectangle2D getBoundingBox() {
            return new Rectangle2D.Double(0, 0, 1, 1);
        }

        @Override
        public boolean hitTest(Point2D point) {
            return true;
        }

        @Override
        public Point2D getCenter() {
            return new Point2D.Double(0, 0);
        }
    }

    @Test
    void testHitDeepestSkipNulls() {
        final HitableNode toHit = new HitableNode();
        Node node = new DummyNode(
                new Node[]{
                        new DummyNode(new Node[]{toHit }),
                        new DummyNode(),
                }
        );
        Assertions.assertSame(toHit, HitMan.hitDeepest(new Point2D.Double(0.5, 0.5), node, HitableNode.class));
    }

}
