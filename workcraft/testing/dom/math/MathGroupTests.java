package org.workcraft.testing.dom.math;

import org.junit.Test;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchyObserver;
import org.workcraft.framework.observation.NodesAddedEvent;
import org.workcraft.framework.observation.NodesDeletedEvent;

import static org.junit.Assert.*;

public class MathGroupTests {
	class MockNode extends MathNode {

	}

	private boolean receivedAddNotification1 = false;
	private boolean receivedAddNotification2 = false;
	private boolean receivedRemoveNotification1 = false;
	private boolean receivedRemoveNotification2 = false;

	private MockNode n1 = new MockNode();
	private MockNode n2 = new MockNode();

	private MathGroup group;
	private MathGroup group2;

	@Test
	public void ObservationTest() {
		group = new MathGroup();

		group.addObserver(new HierarchyObserver() {
			public void notify(HierarchyEvent e) {
				if (e instanceof NodesAddedEvent) {
					if (e.getAffectedNodes().iterator().next() == n1)
						receivedAddNotification1 = true;
					else if (e.getAffectedNodes().iterator().next() == n2)
						receivedAddNotification2 = true;
				} else if (e instanceof NodesDeletedEvent) {
					if (e.getAffectedNodes().iterator().next() == n1)
						receivedRemoveNotification1 = true;
					else if (e.getAffectedNodes().iterator().next() == n2)
						receivedRemoveNotification2 = true;
				}
			}
		});

		group.add(n1);
		assertTrue(receivedAddNotification1);
		group.add(n2);
		assertTrue(receivedAddNotification2);

		assertEquals(group.getChildren().size(), 2);

		group.remove(n2);
		assertTrue(receivedRemoveNotification2);
		group.remove(n1);
		assertTrue(receivedRemoveNotification1);

		assertEquals(group.getChildren().size(), 0);

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
