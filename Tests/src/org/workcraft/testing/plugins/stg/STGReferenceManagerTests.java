package org.workcraft.testing.plugins.stg;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.plugins.stg.STGReferenceManager;
import org.workcraft.plugins.stg.SignalTransition;

public class STGReferenceManagerTests {

	@Test
	public void testGenerateSignalName() {
		MathGroup root = new MathGroup();
		SignalTransition transition = new SignalTransition();
		root.add(transition);
		STGReferenceManager mgr = new STGReferenceManager(null);
		mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition})));
		Assert.assertEquals("t", transition.getSignalName());
	}
	@Test
	public void testGenerateSignalNameFromNull() {
		MathGroup root = new MathGroup();
		SignalTransition transition = new SignalTransition();
		root.add(transition);
		transition.setSignalName(null);
		STGReferenceManager mgr = new STGReferenceManager(null);
		mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition})));
		Assert.assertEquals("t", transition.getSignalName());
	}
	@Test
	public void testGenerateSignalNameFromEmpty() {
		MathGroup root = new MathGroup();
		SignalTransition transition = new SignalTransition();
		root.add(transition);
		transition.setSignalName("");
		STGReferenceManager mgr = new STGReferenceManager(null);
		mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition})));
		Assert.assertEquals("t", transition.getSignalName());
	}
	@Test
	public void testGenerateSignalNameTwice() {
		MathGroup root = new MathGroup();
		SignalTransition transition1 = new SignalTransition();
		SignalTransition transition2 = new SignalTransition();
		root.add(transition1);
		root.add(transition2);
		STGReferenceManager mgr = new STGReferenceManager(null);
		mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition1})));
		mgr.handleEvent(new NodesAddedEvent(null, Arrays.asList(new Node[]{transition2})));
		Assert.assertEquals("t", transition1.getSignalName());
		Assert.assertEquals("t", transition2.getSignalName());
	}
}
