package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Variable;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

public class Variable_NoDataPath extends
		ComponentStgBuilder<Variable> {

	public Variable_NoDataPath()
	{
	}

	public void buildStg(Variable component, Map<String, Process> handshakes, StgBuilder builder) {
		// No action for reading needed, so we do nothing for read handshakes

		//Need latch on writing
		PassivePushStg write = (PassivePushStg)handshakes.get("write");

		StgSignal latchRq = builder.buildSignal(new SignalId(component, "latchRq"), true);
		StgSignal latchAc = builder.buildSignal(new SignalId(component, "latchAc"), false);
		OutputPlace latchReady = builder.buildPlace(1);
		builder.connect(latchReady, latchRq.getPlus());
		builder.connect(latchRq.getPlus(), latchAc.getPlus());
		builder.connect(latchAc.getPlus(), latchRq.getMinus());
		builder.connect(latchRq.getMinus(), latchAc.getMinus());
		builder.connect(latchAc.getMinus(), latchReady);

		builder.connect(write.go(), latchRq.getPlus());
		builder.connect(latchAc.getPlus(), write.dataRelease());
	}
}
