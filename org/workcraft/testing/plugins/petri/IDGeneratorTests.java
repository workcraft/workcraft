package org.workcraft.testing.plugins.petri;

import static org.junit.Assert.*;

import org.junit.Test;
import org.workcraft.dom.references.IDGenerator;
import org.workcraft.exceptions.DuplicateIDException;

public class IDGeneratorTests {

	@Test
	public void testReserveID() {
		IDGenerator generator = new IDGenerator();
		generator.reserveID(8);
		assertEquals(0, generator.getNextID());
		for (int i=1; i<8; i++)
			assertEquals(i, generator.getNextID());
		assertEquals(9, generator.getNextID());
	}

	@Test
	public void testReleaseID() {
		IDGenerator generator = new IDGenerator();
		generator.reserveID(8);
		assertEquals(0, generator.getNextID());
		for (int i=1; i<8; i++)
			assertEquals(i, generator.getNextID());
		assertEquals(9, generator.getNextID());

		generator.releaseID(4);
		assertEquals(4, generator.getNextID());
		assertEquals(10, generator.getNextID());

		try {
		 generator.reserveID(4);
		 fail ("Duplicate ID issued");
		} catch (DuplicateIDException e) {
			assertEquals(4, e.getId());
		}

		generator.releaseID(4);
		generator.releaseID(0);

		assertEquals(0, generator.getNextID());
		generator.reserveID(4);
		assertEquals(11, generator.getNextID());
	}

	@Test
	public void testGetNextID() {
		IDGenerator generator = new IDGenerator();
		assertEquals(0, generator.getNextID());
		assertEquals(1, generator.getNextID());
	}
}
