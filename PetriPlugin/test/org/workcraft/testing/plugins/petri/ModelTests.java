package org.workcraft.testing.plugins.petri;

import static org.junit.Assert.*;

import org.junit.Test;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;

public class ModelTests {

    @Test
    public void testTransitionsAndPlacesCollections() {
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
