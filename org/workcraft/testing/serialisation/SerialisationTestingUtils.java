/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.testing.serialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Iterator;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.ImplicitPlaceArc;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SerialisationTestingUtils {
	public static void comparePlaces (Place p1, Place p2) {
		assertEquals(p1.getTokens(), p2.getTokens());
		assertEquals(p1.getLabel(), p2.getLabel());
		assertEquals(p1.getCapacity(), p2.getCapacity());
		//assertEquals(p1.getID(), p2.getID());
	}

	public static void compareTransitions (SignalTransition t1, SignalTransition t2) {
		assertEquals(t1.getLabel(), t2.getLabel());
		assertEquals(t1.getSignalName(), t2.getSignalName());
		assertEquals(t1.getDirection(), t2.getDirection());
		assertEquals(t1.getInstance(), t2.getInstance());
		//assertEquals(t1.getID(), t2.getID());
	}

	public static void compareConnections (MathConnection con1, MathConnection con2) {
		//assertEquals(con1.getID(), con2.getID());
		assertEquals(con1.getLabel(), con2.getLabel());

		compareNodes (con1.getFirst(), con2.getFirst());
		compareNodes (con1.getSecond(), con2.getSecond());
	}

	public static void comparePreAndPostSets(VisualComponent c1, VisualComponent c2) {
	/*	assertEquals(c1.getPreset().size(),c2.getPreset().size());
		assertEquals(c1.getPostset().size(),c2.getPostset().size());

		Iterator<VisualComponent> i1 = c1.getPreset().iterator();
		Iterator<VisualComponent> i2 = c2.getPreset().iterator();

		while ( i1.hasNext() ) {
			VisualComponent n1 = i1.next();
			VisualComponent n2 = i2.next();

			assertEquals (n1.getClass(), n2.getClass());
		}

		i1 = c1.getPostset().iterator();
		i2 = c2.getPostset().iterator();

		while ( i1.hasNext() ) {
			VisualComponent n1 = i1.next();
			VisualComponent n2 = i2.next();

			assertEquals (n1.getClass(), n2.getClass());
		}*/
	}

	public static void comparePreAndPostSets( MathNode c1, MathNode c2) {
		/*assertEquals(c1.getPreset().size(),c2.getPreset().size());
		assertEquals(c1.getPostset().size(),c2.getPostset().size());

		Iterator<MathComponent> i1 = c1.getPreset().iterator();
		Iterator<MathComponent> i2 = c2.getPreset().iterator();

		while ( i1.hasNext() ) {
			Component n1 = i1.next();
			Component n2 = i2.next();

			assertEquals (n1.getClass(), n2.getClass());
		}

		i1 = c1.getPostset().iterator();
		i2 = c2.getPostset().iterator();

		while ( i1.hasNext() ) {
			Component n1 = i1.next();
			Component n2 = i2.next();

			assertEquals (n1.getClass(), n2.getClass());
		}*/
	}

	public static void compareVisualPlaces (VisualPlace p1, VisualPlace p2) {
		//assertEquals(p1.getID(), p2.getID());
		assertEquals(p1.getTransform(), p2.getTransform());

		comparePlaces (p1.getReferencedPlace(), p2.getReferencedPlace());
	}

	public static void compareVisualSignalTransitions (VisualSignalTransition t1, VisualSignalTransition t2) {
		//assertEquals(t1.getID(), t2.getID());
		assertEquals(t1.getTransform(), t2.getTransform());

		compareTransitions (t1.getReferencedTransition(), t2.getReferencedTransition());
	}

	public static void compareVisualConnections (VisualConnection vc1, VisualConnection vc2) {
		compareNodes (vc1.getFirst(), vc2.getFirst());
		compareNodes (vc1.getSecond(), vc2.getSecond());

		compareConnections (vc1.getReferencedConnection(), vc2.getReferencedConnection());
	}

	public static void compareImplicitPlaceArcs (ImplicitPlaceArc vc1, ImplicitPlaceArc vc2) {
		compareNodes (vc1.getFirst(), vc2.getFirst());
		compareNodes (vc1.getSecond(), vc2.getSecond());
		comparePlaces (vc1.getImplicitPlace(), vc2.getImplicitPlace());
		compareConnections (vc1.getRefCon1(), vc2.getRefCon1());
		compareConnections (vc1.getRefCon2(), vc2.getRefCon2());
	}

	public static void compareNodes (Node node1, Node node2) {
		assertEquals(node1.getClass(), node2.getClass());

		if (node1 instanceof MathNode)
			comparePreAndPostSets( (MathNode) node1, (MathNode) node2 );
		else if (node1 instanceof VisualComponent)
			comparePreAndPostSets( (VisualComponent) node1, (VisualComponent) node2 );

		if (node1 instanceof Place)
			comparePlaces ((Place)node1, (Place)node2);
		else if (node1 instanceof MathConnection)
			compareConnections ( (MathConnection)node1, (MathConnection)node2 );
		else if (node1 instanceof SignalTransition)
			compareTransitions ( (SignalTransition)node1, (SignalTransition)node2 );
		else if (node1 instanceof VisualPlace)
			compareVisualPlaces ( (VisualPlace)node1, (VisualPlace)node2 );
		else if (node1 instanceof VisualSignalTransition)
			compareVisualSignalTransitions ( (VisualSignalTransition)node1, (VisualSignalTransition)node2 );
		else if (node1 instanceof ImplicitPlaceArc)
			compareImplicitPlaceArcs ( (ImplicitPlaceArc)node1, (ImplicitPlaceArc)node2 );
		else if (node1 instanceof VisualConnection)
			compareVisualConnections ( (VisualConnection)node1, (VisualConnection)node2 );
		else if (node1 instanceof MathGroup);
		else if (node1 instanceof VisualGroup);
		else
			fail("Unexpected class" + node1.getClass().getName());

		Collection<Node> ch1 = node1.getChildren();
		Collection<Node> ch2 = node2.getChildren();

		assertEquals(ch1.size(), ch2.size());

		Iterator<Node> i1 = ch1.iterator();
		Iterator<Node> i2 = ch2.iterator();

		while ( i1.hasNext() ) {
			Node n1 = i1.next();
			Node n2 = i2.next();

			compareNodes (n1, n2);
		}
	}

	//public
}
