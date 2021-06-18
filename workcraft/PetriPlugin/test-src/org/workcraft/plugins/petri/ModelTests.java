package org.workcraft.plugins.petri;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModelTests {

    @Test
    void testTransitionsAndPlacesCollections() {
        Petri petriNet = new Petri();

        Transition tr = new Transition();
        Assertions.assertEquals(0, petriNet.getTransitions().size());
        petriNet.add(tr);
        Assertions.assertEquals(1, petriNet.getTransitions().size());
        Assertions.assertTrue(petriNet.getTransitions().contains(tr));

        Place pl = new Place();
        Assertions.assertEquals(0, petriNet.getPlaces().size());
        petriNet.add(pl);
        Assertions.assertEquals(1, petriNet.getPlaces().size());
        Assertions.assertTrue(petriNet.getPlaces().contains(pl));
    }

}
