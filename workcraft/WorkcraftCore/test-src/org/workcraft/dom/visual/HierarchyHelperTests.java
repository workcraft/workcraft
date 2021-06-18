package org.workcraft.dom.visual;

import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.Node;
import org.workcraft.utils.Hierarchy;

class HierarchyHelperTests {

    static class MockHierarchyNode implements Node {
        private final Node parent;
        MockHierarchyNode(Node parent) {
            this.parent = parent;
        }
        @Override
        public Collection<Node> getChildren() {
            throw new RuntimeException("not implemented");
        }
        @Override
        public Node getParent() {
            return parent;
        }
        @Override
        public void setParent(Node parent) {
            throw new RuntimeException("not implemented");
        }
    }

    @Test
    void testGetPath() {
        MockHierarchyNode node1 = new MockHierarchyNode(null);
        MockHierarchyNode node2 = new MockHierarchyNode(node1);
        MockHierarchyNode node3 = new MockHierarchyNode(node2);
        MockHierarchyNode node3b = new MockHierarchyNode(node2);

        Assertions.assertArrayEquals(new Node[]{node1}, Hierarchy.getPath(node1));
        Assertions.assertArrayEquals(new Node[]{node1, node2}, Hierarchy.getPath(node2));
        Assertions.assertArrayEquals(new Node[]{node1, node2, node3}, Hierarchy.getPath(node3));
        Assertions.assertArrayEquals(new Node[]{node1, node2, node3b}, Hierarchy.getPath(node3b));
    }

}
