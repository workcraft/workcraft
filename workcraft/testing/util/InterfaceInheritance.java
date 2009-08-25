package org.workcraft.testing.util;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dom.XMLSerialisable;
import org.workcraft.plugins.stg.SignalTransition;

interface I {

}

class AA implements I {

}

class BB extends AA {

}


public class InterfaceInheritance {
	@Test
	public void test() {
		Assert.assertTrue(new AA() instanceof I);
		Assert.assertTrue(((Object)new BB()) instanceof I);

		Assert.assertTrue(new SignalTransition() instanceof XMLSerialisable);
	}
}
