package org.workcraft.plugins.stg;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.math.PageNode;

public class StgReparentTests {

    @Test
    public void testDummyReparent() {
        Stg stg = new Stg();

        StgPlace place = stg.createPlace("ABC", stg.getRoot());
        Assert.assertEquals("ABC", stg.getNodeReference(place));

        // Cannot create transition with the same name as an existing place.
        SignalTransition transition = null;
        try {
            transition = stg.createSignalTransition("ABC", stg.getRoot());
            Assert.fail("Unexpected assignemnt of name 'ABC' that is already taken.");
        } catch (Exception e) {
        }
        Assert.assertNull(transition);

        PageNode page1 = new PageNode();
        stg.add(page1);
        stg.setName(page1, "page1");
        DummyTransition dummy1 = stg.createDummyTransition("ABC", page1);

        Assert.assertEquals("page1.ABC", stg.getNodeReference(dummy1));

        PageNode page2 = new PageNode();
        stg.add(page2);
        stg.setName(page2, "page2");

        DummyTransition dummy2 = stg.createDummyTransition("ABC", page2);
        Assert.assertEquals("page2.ABC", stg.getNodeReference(dummy2));

        boolean dummy1ReparentResult = stg.reparent(stg.getRoot(), stg, page1, Arrays.asList(dummy1));
        Assert.assertTrue(dummy1ReparentResult);
        Assert.assertEquals("ABCa", stg.getNodeReference(dummy1));

        boolean dummy2ReparentResult = stg.reparent(stg.getRoot(), stg, page2, Arrays.asList(dummy2));
        Assert.assertTrue(dummy2ReparentResult);
        Assert.assertEquals("ABCb", stg.getNodeReference(dummy2));
    }

    @Test
    public void testPlaceReparent() {
        Stg stg = new Stg();

        DummyTransition dummy = stg.createDummyTransition("ABC", stg.getRoot());
        Assert.assertEquals("ABC", stg.getNodeReference(dummy));

        // Cannot create a transition with the same name name as an existing dummy.
        SignalTransition transition = null;
        try {
            transition = stg.createSignalTransition("ABC", stg.getRoot());
            Assert.fail("Unexpected assignemnt of name 'ABC' that is already taken.");
        } catch (Exception e) {
        }
        Assert.assertNull(transition);

        PageNode page1 = new PageNode();
        stg.add(page1);
        stg.setName(page1, "page1");
        StgPlace place1 = stg.createPlace("ABC", page1);

        Assert.assertEquals("page1.ABC", stg.getNodeReference(place1));

        PageNode page2 = new PageNode();
        stg.add(page2);
        stg.setName(page2, "page2");

        StgPlace place2 = stg.createPlace("ABC", page2);
        Assert.assertEquals("page2.ABC", stg.getNodeReference(place2));

        boolean place1ReparentResult = stg.reparent(stg.getRoot(), stg, page1, Arrays.asList(place1));
        Assert.assertTrue(place1ReparentResult);
        Assert.assertEquals("ABCa", stg.getNodeReference(place1));

        boolean place2ReparentResult = stg.reparent(stg.getRoot(), stg, page2, Arrays.asList(place2));
        Assert.assertTrue(place2ReparentResult);
        Assert.assertEquals("ABCb", stg.getNodeReference(place2));
    }

    @Test
    public void testTransitionReparent() {
        Stg stg = new Stg();

        DummyTransition dummy = stg.createDummyTransition("ABC", stg.getRoot());
        Assert.assertEquals("ABC", stg.getNodeReference(dummy));

        // Cannot create a place with the same name as an existing dummy.
        StgPlace place = null;
        try {
            place = stg.createPlace("ABC", stg.getRoot());
            Assert.fail("Unexpected assignemnt of name 'ABC' that is already taken.");
        } catch (Exception e) {
        }
        Assert.assertNull(place);

        PageNode page1 = new PageNode();
        stg.add(page1);
        stg.setName(page1, "page1");
        SignalTransition transition1 = stg.createSignalTransition("ABC+", page1);
        transition1.setSignalType(Signal.Type.INPUT);
        Assert.assertEquals("page1.ABC+", stg.getNodeReference(transition1));

        PageNode page2 = new PageNode();
        stg.add(page2);
        stg.setName(page2, "page2");
        SignalTransition transition2 = stg.createSignalTransition("ABC-", page2);
        transition2.setSignalType(Signal.Type.INPUT);
        Assert.assertEquals("page2.ABC-", stg.getNodeReference(transition2));

        PageNode page3 = new PageNode();
        stg.add(page3);
        stg.setName(page3, "page3");
        SignalTransition transition3 = stg.createSignalTransition("XYZ", page3);
        transition3.setSignalType(Signal.Type.OUTPUT);
        Assert.assertEquals("page3.XYZ~", stg.getNodeReference(transition3));

        PageNode page4 = new PageNode();
        stg.add(page4);
        stg.setName(page4, "page4");
        SignalTransition transition4 = stg.createSignalTransition("XYZ", page4);
        transition4.setSignalType(Signal.Type.INTERNAL);
        Assert.assertEquals("page4.XYZ~", stg.getNodeReference(transition4));

        boolean transition1ReparentResult = stg.reparent(stg.getRoot(), stg, page1, Arrays.asList(transition1));
        Assert.assertFalse(transition1ReparentResult);

        boolean transition2ReparentResult = stg.reparent(stg.getRoot(), stg, page2, Arrays.asList(transition2));
        Assert.assertFalse(transition2ReparentResult);

        boolean transition3ReparentResult = stg.reparent(stg.getRoot(), stg, page3, Arrays.asList(transition3));
        Assert.assertTrue(transition3ReparentResult);

        boolean transition4ReparentResult = stg.reparent(stg.getRoot(), stg, page4, Arrays.asList(transition4));
        Assert.assertFalse(transition4ReparentResult);
    }

}