package org.workcraft.testing.serialisation;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Group;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SerialisationTestingUtils {
	public static void comparePlaces (Place p1, Place p2) {
		assertEquals(p1.getTokens(), p2.getTokens());
		assertEquals(p1.getLabel(), p2.getLabel());
		assertEquals(p1.getCapacity(), p2.getCapacity());
		assertEquals(p1.getID(), p2.getID());
	}

	public static void compareTransitions (SignalTransition t1, SignalTransition t2) {
		assertEquals(t1.getLabel(), t2.getLabel());
		assertEquals(t1.getSignalName(), t2.getSignalName());
		assertEquals(t1.getDirection(), t2.getDirection());
		assertEquals(t1.getInstance(), t2.getInstance());
		assertEquals(t1.getID(), t2.getID());
	}

	public static void compareConnections (Connection con1, Connection con2) {
		assertEquals(con1.getID(), con2.getID());
		assertEquals(con1.getLabel(), con2.getLabel());

		compareNodes (con1.getFirst(), con2.getFirst());
		compareNodes (con1.getSecond(), con2.getSecond());
	}

	public static void compareVisualPlaces (VisualPlace p1, VisualPlace p2) {
		assertEquals(p1.getID(), p2.getID());
		assertEquals(p1.getTransform(), p2.getTransform());

		comparePlaces (p1.getReferencedPlace(), p2.getReferencedPlace());
	}

	public static void compareVisualSignalTransitions (VisualSignalTransition t1, VisualSignalTransition t2) {
		assertEquals(t1.getID(), t2.getID());
		assertEquals(t1.getTransform(), t2.getTransform());

		compareTransitions (t1.getReferencedTransition(), t2.getReferencedTransition());
	}

	public static void compareVisualConnections (VisualConnection vc1, VisualConnection vc2) {
		assertEquals(vc1.getID(), vc2.getID());

		compareConnections (vc1.getReferencedConnection(), vc2.getReferencedConnection());
	}

	public static void compareNodes (HierarchyNode node1, HierarchyNode node2) {
		assertEquals(node1.getClass(), node2.getClass());

		if (node1 instanceof Place)
			comparePlaces ((Place)node1, (Place)node2);
		else if (node1 instanceof Connection)
			compareConnections ( (Connection)node1, (Connection)node2 );
		else if (node1 instanceof SignalTransition)
			compareTransitions ( (SignalTransition)node1, (SignalTransition)node2 );
		else if (node1 instanceof VisualPlace)
			compareVisualPlaces ( (VisualPlace)node1, (VisualPlace)node2 );
		else if (node1 instanceof VisualSignalTransition)
			compareVisualSignalTransitions ( (VisualSignalTransition)node1, (VisualSignalTransition)node2 );
		else if (node1 instanceof VisualConnection)
			compareVisualConnections ( (VisualConnection)node1, (VisualConnection)node2 );
		else if (node1 instanceof Group);
		else if (node1 instanceof VisualGroup);
		else
			fail("Unexpected class" + node1.getClass().getName());

		Collection<HierarchyNode> ch1 = node1.getChildren();
		Collection<HierarchyNode> ch2 = node2.getChildren();

		assertEquals(ch1.size(), ch2.size());

		Iterator<HierarchyNode> i1 = ch1.iterator();
		Iterator<HierarchyNode> i2 = ch2.iterator();

		while ( i1.hasNext() ) {
			HierarchyNode n1 = i1.next();
			HierarchyNode n2 = i2.next();

			compareNodes (n1, n2);
		}
	}
}
