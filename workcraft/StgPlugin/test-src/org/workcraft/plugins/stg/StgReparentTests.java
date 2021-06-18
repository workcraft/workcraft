package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.math.PageNode;

import java.util.Arrays;

class StgReparentTests {

    @Test
    void testDummyReparent() {
        Stg stg = new Stg();

        StgPlace place = stg.createPlace("ABC", stg.getRoot());
        Assertions.assertEquals("ABC", stg.getNodeReference(place));

        // Cannot create transition with the same name as an existing place.
        SignalTransition transition = stg.createSignalTransition("ABC", stg.getRoot());
        Assertions.assertEquals("ABCa", stg.getNodeReference(place));
        Assertions.assertEquals("ABC~", stg.getNodeReference(transition));

        PageNode page1 = new PageNode();
        stg.add(page1);
        stg.setName(page1, "page1");
        DummyTransition dummy1 = stg.createDummyTransition("ABC", page1);

        Assertions.assertEquals("page1.ABC", stg.getNodeReference(dummy1));

        PageNode page2 = new PageNode();
        stg.add(page2);
        stg.setName(page2, "page2");

        DummyTransition dummy2 = stg.createDummyTransition("ABC", page2);
        Assertions.assertEquals("page2.ABC", stg.getNodeReference(dummy2));

        boolean dummy1ReparentResult = stg.reparent(stg.getRoot(), stg, page1, Arrays.asList(dummy1));
        Assertions.assertTrue(dummy1ReparentResult);
        Assertions.assertEquals("ABCb", stg.getNodeReference(dummy1));

        boolean dummy2ReparentResult = stg.reparent(stg.getRoot(), stg, page2, Arrays.asList(dummy2));
        Assertions.assertTrue(dummy2ReparentResult);
        Assertions.assertEquals("ABCc", stg.getNodeReference(dummy2));
    }

    @Test
    void testPlaceReparent() {
        Stg stg = new Stg();

        DummyTransition dummy = stg.createDummyTransition("ABC", stg.getRoot());
        Assertions.assertEquals("ABC", stg.getNodeReference(dummy));

        // Cannot create a transition with the same name name as an existing dummy.
        SignalTransition transition = null;
        try {
            transition = stg.createSignalTransition("ABC", stg.getRoot());
            Assertions.fail("Unexpected assignemnt of name 'ABC' that is already taken.");
        } catch (Exception e) {
        }
        Assertions.assertNull(transition);

        PageNode page1 = new PageNode();
        stg.add(page1);
        stg.setName(page1, "page1");
        StgPlace place1 = stg.createPlace("ABC", page1);

        Assertions.assertEquals("page1.ABC", stg.getNodeReference(place1));

        PageNode page2 = new PageNode();
        stg.add(page2);
        stg.setName(page2, "page2");

        StgPlace place2 = stg.createPlace("ABC", page2);
        Assertions.assertEquals("page2.ABC", stg.getNodeReference(place2));

        boolean place1ReparentResult = stg.reparent(stg.getRoot(), stg, page1, Arrays.asList(place1));
        Assertions.assertTrue(place1ReparentResult);
        Assertions.assertEquals("ABCa", stg.getNodeReference(place1));

        boolean place2ReparentResult = stg.reparent(stg.getRoot(), stg, page2, Arrays.asList(place2));
        Assertions.assertTrue(place2ReparentResult);
        Assertions.assertEquals("ABCb", stg.getNodeReference(place2));
    }

    @Test
    void testTransitionReparent() {
        Stg stg = new Stg();

        DummyTransition dummy = stg.createDummyTransition("ABC", stg.getRoot());
        Assertions.assertEquals("ABC", stg.getNodeReference(dummy));

        // Cannot create a place with the same name as an existing dummy.
        StgPlace place = null;
        try {
            place = stg.createPlace("ABC", stg.getRoot());
            Assertions.fail("Unexpected assignemnt of name 'ABC' that is already taken.");
        } catch (Exception e) {
        }
        Assertions.assertNull(place);

        PageNode page1 = new PageNode();
        stg.add(page1);
        stg.setName(page1, "page1");
        SignalTransition transition1 = stg.createSignalTransition("ABC+", page1);
        transition1.setSignalType(Signal.Type.INPUT);
        Assertions.assertEquals("page1.ABC+", stg.getNodeReference(transition1));

        PageNode page2 = new PageNode();
        stg.add(page2);
        stg.setName(page2, "page2");
        SignalTransition transition2 = stg.createSignalTransition("ABC-", page2);
        transition2.setSignalType(Signal.Type.INPUT);
        Assertions.assertEquals("page2.ABC-", stg.getNodeReference(transition2));

        PageNode page3 = new PageNode();
        stg.add(page3);
        stg.setName(page3, "page3");
        SignalTransition transition3 = stg.createSignalTransition("XYZ", page3);
        transition3.setSignalType(Signal.Type.OUTPUT);
        Assertions.assertEquals("page3.XYZ~", stg.getNodeReference(transition3));

        PageNode page4 = new PageNode();
        stg.add(page4);
        stg.setName(page4, "page4");
        SignalTransition transition4 = stg.createSignalTransition("XYZ", page4);
        transition4.setSignalType(Signal.Type.INTERNAL);
        Assertions.assertEquals("page4.XYZ~", stg.getNodeReference(transition4));

        boolean transition1ReparentResult = stg.reparent(stg.getRoot(), stg, page1, Arrays.asList(transition1));
        Assertions.assertFalse(transition1ReparentResult);

        boolean transition2ReparentResult = stg.reparent(stg.getRoot(), stg, page2, Arrays.asList(transition2));
        Assertions.assertFalse(transition2ReparentResult);

        boolean transition3ReparentResult = stg.reparent(stg.getRoot(), stg, page3, Arrays.asList(transition3));
        Assertions.assertTrue(transition3ReparentResult);

        boolean transition4ReparentResult = stg.reparent(stg.getRoot(), stg, page4, Arrays.asList(transition4));
        Assertions.assertFalse(transition4ReparentResult);
    }

}