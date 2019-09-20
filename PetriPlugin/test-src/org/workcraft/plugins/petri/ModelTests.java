package org.workcraft.plugins.petri;

import org.junit.Assert;
import org.junit.Test;

public class ModelTests {

    @Test
    public void testTransitionsAndPlacesCollections() {
        Petri petriNet = new Petri();

        Transition tr = new Transition();
        Assert.assertEquals(0, petriNet.getTransitions().size());
        petriNet.add(tr);
        Assert.assertEquals(1, petriNet.getTransitions().size());
        Assert.assertTrue(petriNet.getTransitions().contains(tr));

        Place pl = new Place();
        Assert.assertEquals(0, petriNet.getPlaces().size());
        petriNet.add(pl);
        Assert.assertEquals(1, petriNet.getPlaces().size());
        Assert.assertTrue(petriNet.getPlaces().contains(pl));
    }

}
