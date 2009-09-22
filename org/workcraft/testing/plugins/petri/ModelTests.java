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

package org.workcraft.testing.plugins.petri;

import static org.junit.Assert.*;

import org.junit.Test;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;

public class ModelTests {

	@Test
	public void TestTransitionsAndPlacesCollections()
	{
		PetriNet petriNet = new PetriNet();

		Transition tr = new Transition();
		assertEquals(0, petriNet.getTransitions().size());
		petriNet.add(tr);
		assertEquals(1, petriNet.getTransitions().size());
		assertTrue(petriNet.getTransitions().contains(tr));

		Place pl = new Place();
		assertEquals(0, petriNet.getPlaces().size());
		petriNet.add(pl);
		assertEquals(1, petriNet.getPlaces().size());
		assertTrue(petriNet.getPlaces().contains(pl));
	}
}
