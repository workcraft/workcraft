package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Variable;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

public class Variable_NoDataPath extends
		ComponentStgBuilder<Variable> {

	public Variable_NoDataPath()
	{
	}

	public void buildStg(Variable component, Map<String, StgHandshake> handshakes, StgBuilder builder) {
		// No action for reading needed, so we do nothing for read handshakes

		//Need latch on writing
		PassivePushStg write = (PassivePushStg)handshakes.get("write");

		StgSignal latchRq = builder.buildSignal(new SignalId(component, "latchRq"), true);
		StgSignal latchAc = builder.buildSignal(new SignalId(component, "latchAc"), false);
		StgPlace latchReady = builder.buildPlace(1);
		builder.addConnection(latchReady, latchRq.getPlus());
		builder.addConnection(latchRq.getPlus(), latchAc.getPlus());
		builder.addConnection(latchAc.getPlus(), latchRq.getMinus());
		builder.addConnection(latchRq.getMinus(), latchAc.getMinus());
		builder.addConnection(latchAc.getMinus(), latchReady);

		builder.addConnection(write.getActivate(), latchRq.getPlus());
		builder.addConnection(latchAc.getPlus(), write.getDataReleased());
	}
}
