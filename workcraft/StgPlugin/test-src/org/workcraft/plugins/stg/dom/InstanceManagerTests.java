package org.workcraft.plugins.stg.dom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.DefaultNodeImpl;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.stg.references.InstanceManager;
import org.workcraft.types.Pair;

import java.util.HashMap;
import java.util.Map;

class InstanceManagerTests {

    @Test
    void testConstructor() {
        new InstanceManager() {
            @Override
            public String getLabel(Node node) {
                throw new RuntimeException("this method should not be called");
            }
        };
    }

    private InstanceManager make(final Map<Node, String> expectedRequests) {
        return new InstanceManager() {
            @Override
            public String getLabel(Node node) {
                final String label = expectedRequests.get(node);
                if (label == null) {
                    throw new RuntimeException("unexpected request: " + node);
                }
                return label;
            }
        };
    }

    @Test
    void testGetReferenceUnknown() {
        Map<Node, String> expectedRequests = new HashMap<>();
        final InstanceManager mgr = make(expectedRequests);
        Assertions.assertNull(mgr.getInstance(new DefaultNodeImpl(null)));
    }

    @Test
    void testAssign() {
        Map<Node, String> expectedRequests = new HashMap<>();
        Node o1 = new DefaultNodeImpl(null);
        Node o2 = new DefaultNodeImpl(null);
        Node o3 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        expectedRequests.put(o2, "abc");
        expectedRequests.put(o3, "qwe");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1);
        mgr.assign(o2);
        mgr.assign(o3);
        Assertions.assertEquals(Pair.of("abc", 1), mgr.getInstance(o2));
        Assertions.assertEquals(Pair.of("qwe", 0), mgr.getInstance(o3));
        Assertions.assertEquals(Pair.of("abc", 0), mgr.getInstance(o1));
    }

    @Test
    void testAssignAfterRemove() {
        Map<Node, String> expectedRequests = new HashMap<>();
        Node o1 = new DefaultNodeImpl(null);
        Node o2 = new DefaultNodeImpl(null);
        Node o3 = new DefaultNodeImpl(null);
        Node o4 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        expectedRequests.put(o2, "abc");
        expectedRequests.put(o3, "qwe");
        expectedRequests.put(o4, "abc");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1);
        mgr.assign(o2);
        mgr.assign(o3);
        Assertions.assertEquals(Pair.of("abc", 1), mgr.getInstance(o2));
        Assertions.assertEquals(Pair.of("qwe", 0), mgr.getInstance(o3));
        Assertions.assertEquals(Pair.of("abc", 0), mgr.getInstance(o1));

        mgr.assign(o2, 1);
        mgr.assign(o2, 2);
        mgr.assign(o4);

        Assertions.assertEquals(Pair.of("abc", 1), mgr.getInstance(o4));
    }

    @Test
    void testRemove() {
        Map<Node, String> expectedRequests = new HashMap<>();
        Node o1 = new DefaultNodeImpl(null);
        Node o2 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        expectedRequests.put(o2, "abc");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1);
        mgr.remove(o1);
        mgr.assign(o2);
        Assertions.assertEquals(Pair.of("abc", 0), mgr.getInstance(o2));
        Assertions.assertNull(mgr.getInstance(o1));
    }

    @Test
    void testDoubleAssign() {
        Map<Node, String> expectedRequests = new HashMap<>();
        Node o1 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1);
        Assertions.assertThrows(ArgumentException.class, () -> mgr.assign(o1));
    }

    @Test
    void testAssignForced() {
        Map<Node, String> expectedRequests = new HashMap<>();
        Node o1 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1, 8);
        Assertions.assertEquals(Pair.of("abc", 8), mgr.getInstance(o1));
    }

    @Test
    void testAssignForcedExistingId() {
        Map<Node, String> expectedRequests = new HashMap<>();
        Node o1 = new DefaultNodeImpl(null);
        Node o2 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        expectedRequests.put(o2, "abc");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1, 8);
        Assertions.assertThrows(ArgumentException.class, () -> mgr.assign(o2, 8));
    }

    @Test
    void testNotFound() {
        InstanceManager mgr = new InstanceManager() {
            @Override
            public String getLabel(Node node) {
                return "O_O";
            }
        };

        Assertions.assertNull(mgr.getObject(Pair.of("o_O", 8)));
    }

}
