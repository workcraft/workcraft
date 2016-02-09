package org.workcraft.testing.plugins.petri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.workcraft.dom.DefaultNodeImpl;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.plugins.stg.InstanceManager;
import org.workcraft.util.Pair;

public class InstanceManagerTests
{

    @Test
    public void testConstructor()
    {
        new InstanceManager() {
            @Override public String getLabel(Node node) {
                throw new RuntimeException("this method should not be called");
            }
        };
    }

    InstanceManager make(final Map<Node, String> expectedRequests)
    {
        return new InstanceManager() {
            @Override public String getLabel(Node node) {
                final String label = expectedRequests.get(node);
                if(label==null)
                    throw new RuntimeException("unexpected request: " + node);
                return label;
            }
        };
    }

    @Test
    public void testGetReferenceUnknown()
    {
        Map<Node, String> expectedRequests = new HashMap<Node, String>();
        final InstanceManager mgr = make(expectedRequests);
        assertNull(mgr.getInstance(new DefaultNodeImpl(null)));
    }

    @Test
    public void testAssign()
    {
        Map<Node, String> expectedRequests = new HashMap<Node, String>();
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
        assertEquals(Pair.of("abc",1), mgr.getInstance(o2));
        assertEquals(Pair.of("qwe",0), mgr.getInstance(o3));
        assertEquals(Pair.of("abc",0), mgr.getInstance(o1));
    }

    @Test
    public void testAssignAfterRemove()
    {
        Map<Node, String> expectedRequests = new HashMap<Node, String>();
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
        assertEquals(Pair.of("abc",1), mgr.getInstance(o2));
        assertEquals(Pair.of("qwe",0), mgr.getInstance(o3));
        assertEquals(Pair.of("abc",0), mgr.getInstance(o1));

        mgr.assign(o2, 1);
        mgr.assign(o2, 2);
        mgr.assign(o4);

        assertEquals(Pair.of("abc",1), mgr.getInstance(o4));
    }

    @Test
    public void testRemove()
    {
        Map<Node, String> expectedRequests = new HashMap<Node, String>();
        Node o1 = new DefaultNodeImpl(null);
        Node o2 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        expectedRequests.put(o2, "abc");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1);
        mgr.remove(o1);
        mgr.assign(o2);
        assertEquals(Pair.of("abc",0), mgr.getInstance(o2));
        assertNull(mgr.getInstance(o1));
    }


    @Test(expected=ArgumentException.class)
    public void testDoubleAssign()
    {
        Map<Node, String> expectedRequests = new HashMap<Node, String>();
        Node o1 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1);
        mgr.assign(o1);
    }

    @Test
    public void testAssignForced()
    {
        Map<Node, String> expectedRequests = new HashMap<Node, String>();
        Node o1 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1, 8);
        assertEquals(Pair.of("abc",8), mgr.getInstance(o1));
    }

    @Test(expected=DuplicateIDException.class)
    public void testAssignForcedExistingId()
    {
        Map<Node, String> expectedRequests = new HashMap<Node, String>();
        Node o1 = new DefaultNodeImpl(null);
        Node o2 = new DefaultNodeImpl(null);
        expectedRequests.put(o1, "abc");
        expectedRequests.put(o2, "abc");
        final InstanceManager mgr = make(expectedRequests);
        mgr.assign(o1, 8);
        mgr.assign(o2, 8);
    }

    @Test
    public void testNotFound()
    {
        InstanceManager mgr = new InstanceManager() {
            @Override
            public String getLabel(Node node) {
                return "O_O";
            }
        };

        assertNull(mgr.getObject(Pair.of("o_O", 8)));
    }

}
