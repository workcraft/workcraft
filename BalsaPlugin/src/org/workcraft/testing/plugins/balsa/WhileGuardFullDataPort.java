package org.workcraft.testing.plugins.balsa;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.parsers.breeze.BreezeInstance;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.DefaultBreezeFactory;
import org.workcraft.parsers.breeze.EmptyValueList;
import org.workcraft.parsers.breeze.PrimitivePart;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPull;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakes.MainHandshakeMaker;
import org.workcraft.plugins.balsa.io.BalsaSystem;

public class WhileGuardFullDataPort {
	@Test
	public void test() throws Exception
	{
		PrimitivePart whilE = new BreezeLibrary(BalsaSystem.DEFAULT()).getPrimitive("While");
		BalsaCircuit balsa = new BalsaCircuit();
		BreezeInstance<BreezeHandshake> instance = whilE.instantiate(new DefaultBreezeFactory(balsa), EmptyValueList.instance());

		Map<String, Handshake> handshakes = MainHandshakeMaker.getHandshakes(instance.ports().get(0).getOwner().getUnderlyingComponent());
		Assert.assertTrue("guard is FullDataPull", handshakes.get("guard") instanceof FullDataPull);
		Assert.assertTrue(((FullDataPull)handshakes.get("guard")).isActive());
	}
}
