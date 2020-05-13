package org.workcraft.plugins.stg;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.plugins.stg.references.StgReferenceManager;

public class StgReferenceManagerTests {

    @Test
    public void testGenerateSignalName() {
        MathGroup root = new MathGroup();
        SignalTransition transition = new SignalTransition();
        root.add(transition);
        StgReferenceManager mgr = new StgReferenceManager(null);
        mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition})));
        Assertions.assertEquals("sig", transition.getSignalName());
    }

    @Test
    public void testGenerateSignalNameFromNull() {
        MathGroup root = new MathGroup();
        SignalTransition transition = new SignalTransition();
        root.add(transition);
        transition.setSignalName(null);
        StgReferenceManager mgr = new StgReferenceManager(null);
        mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition})));
        Assertions.assertEquals("sig", transition.getSignalName());
    }

    @Test
    public void testGenerateSignalNameFromEmpty() {
        MathGroup root = new MathGroup();
        SignalTransition transition = new SignalTransition();
        root.add(transition);
        transition.setSignalName("");
        StgReferenceManager mgr = new StgReferenceManager(null);
        mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition})));
        Assertions.assertEquals("sig", transition.getSignalName());
    }

    @Test
    public void testGenerateSignalNameTwice() {
        MathGroup root = new MathGroup();
        SignalTransition transition1 = new SignalTransition();
        SignalTransition transition2 = new SignalTransition();
        root.add(transition1);
        root.add(transition2);
        StgReferenceManager mgr = new StgReferenceManager(null);
        mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition1})));
        mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition2})));
        Assertions.assertEquals("sig", transition1.getSignalName());
        Assertions.assertEquals("sig", transition2.getSignalName());
    }

}
