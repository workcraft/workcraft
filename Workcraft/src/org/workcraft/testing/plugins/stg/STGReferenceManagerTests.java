package org.workcraft.testing.plugins.stg;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dom.Node;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.plugins.stg.STGReferenceManager;
import org.workcraft.plugins.stg.SignalTransition;

public class STGReferenceManagerTests {

	@Test
	public void testGenerateSignalName() {
		SignalTransition transition = new SignalTransition();
		STGReferenceManager refMan = new STGReferenceManager(null);
		refMan.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition})));
		Assert.assertEquals("signal0", transition.getSignalName());
	}
	@Test
	public void testGenerateSignalNameFromNull() {
		SignalTransition transition = new SignalTransition();
		transition.setSignalName(null);
		STGReferenceManager refMan = new STGReferenceManager(null);
		refMan.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition})));
		Assert.assertEquals("signal0", transition.getSignalName());
	}
	@Test
	public void testGenerateSignalNameFromEmpty() {
		SignalTransition transition = new SignalTransition();
		transition.setSignalName("");
		STGReferenceManager refMan = new STGReferenceManager(null);
		refMan.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition})));
		Assert.assertEquals("signal0", transition.getSignalName());
	}
	@Test
	public void testGenerateSignalNameTwice() {
		SignalTransition transition1 = new SignalTransition();
		SignalTransition transition2 = new SignalTransition();
		STGReferenceManager refMan = new STGReferenceManager(null);
		refMan.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition1})));
		refMan.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition2})));
		Assert.assertEquals("signal0", transition1.getSignalName());
		Assert.assertEquals("signal1", transition2.getSignalName());
	}
}
