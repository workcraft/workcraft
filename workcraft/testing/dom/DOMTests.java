package org.workcraft.testing.dom;

import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;

import static org.junit.Assert.*;

public class DOMTests {

	@Test
	public void Test1 () throws InvalidConnectionException {
		PetriNet pn = new PetriNet();

		Place p1 = pn.createPlace();
		Place p2 = pn.createPlace();

		Transition t1 = pn.createTransition();

		Connection con1 = pn.connect(p1, t1);
		Connection con2 = pn.connect(t1, p2);

		assertSame (p1, pn.getNodeByID(pn.getNodeID(p1)));
		assertSame (p2, pn.getNodeByID(pn.getNodeID(p2)));

		assertTrue (pn.getPreset(p2).contains(t1));
		assertTrue (pn.getPostset(p1).contains(t1));

		assertTrue (pn.getConnections(p1).contains(con1));

		pn.remove(p1);

		assertTrue (pn.getConnections(t1).contains(con2));
		assertFalse (pn.getConnections(t1).contains(con1));

		assertEquals(pn.getNodeID(p1), -1);
	}

}
