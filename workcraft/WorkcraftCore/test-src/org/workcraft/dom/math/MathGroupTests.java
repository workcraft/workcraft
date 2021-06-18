package org.workcraft.dom.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;

class MathGroupTests {

    static class MockNode extends MathNode {
    }

    private boolean receivedAddNotification1 = false;
    private boolean receivedAddNotification2 = false;
    private boolean receivedRemoveNotification1 = false;
    private boolean receivedRemoveNotification2 = false;

    private final MockNode n1 = new MockNode();
    private final MockNode n2 = new MockNode();

    @Test
    void observationTest() {
        MathGroup group = new MathGroup();

        group.addObserver((HierarchyObserver) e -> {
            if (e instanceof NodesAddedEvent) {
                if (e.getAffectedNodes().iterator().next() == n1) {
                    receivedAddNotification1 = true;
                } else if (e.getAffectedNodes().iterator().next() == n2) {
                    receivedAddNotification2 = true;
                }
            } else if (e instanceof NodesDeletedEvent) {
                if (e.getAffectedNodes().iterator().next() == n1) {
                    receivedRemoveNotification1 = true;
                } else if (e.getAffectedNodes().iterator().next() == n2) {
                    receivedRemoveNotification2 = true;
                }
            }
        });

        group.add(n1);
        Assertions.assertTrue(receivedAddNotification1);
        group.add(n2);
        Assertions.assertTrue(receivedAddNotification2);

        Assertions.assertEquals(group.getChildren().size(), 2);

        group.remove(n2);
        Assertions.assertTrue(receivedRemoveNotification2);
        group.remove(n1);
        Assertions.assertTrue(receivedRemoveNotification1);

        Assertions.assertEquals(group.getChildren().size(), 0);

        receivedAddNotification1 = false;
        receivedAddNotification2 = false;

        receivedRemoveNotification1 = false;
        receivedRemoveNotification2 = false;

        /*
         * groups no longer forward hierarchy event

        group2 = new MathGroup();
        group.add(group2);

        group2.add(n1);
        group2.add(n2);
        group2.remove(n2);
        group2.remove(n1);

        assertTrue(receivedAddNotification1);
        assertTrue(receivedAddNotification2);

        assertTrue(receivedRemoveNotification2);
        assertTrue(receivedRemoveNotification1); */
    }

}
