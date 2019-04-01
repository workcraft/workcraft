package org.workcraft.plugins.petri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ModelTests {

    @Test
    public void testTransitionsAndPlacesCollections() {
        Petri petriNet = new Petri();

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
