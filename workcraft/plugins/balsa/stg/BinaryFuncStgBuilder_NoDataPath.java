package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

public class BinaryFuncStgBuilder_NoDataPath extends
		ComponentStgBuilder<BinaryFunc> {

	public BinaryFuncStgBuilder_NoDataPath()
	{
	}

	public void buildStg(BinaryFunc component, Map<String, StgHandshake> handshakes, StgBuilder builder) {
		ActivePullStg inpA = (ActivePullStg)handshakes.get("inpA");
		ActivePullStg inpB = (ActivePullStg)handshakes.get("inpB");
		PassivePullStg out = (PassivePullStg)handshakes.get("out");

		StgSignal dpReq = builder.buildSignal(new SignalId(null, "dpReq"), true);
		StgSignal dpAck = builder.buildSignal(new SignalId(null, "dpReq"), true);

		// Read inputs
		builder.addConnection(out.getActivationNotificator(), inpA.getActivator());
		builder.addConnection(out.getActivationNotificator(), inpB.getActivator());

		// When inputs are read, start computing output
		builder.addConnection(inpA.getDeactivationNotificator(), dpReq.getPlus());
		builder.addConnection(inpB.getDeactivationNotificator(), dpReq.getPlus());

		// When output is computed, report to the output
		builder.addConnection(dpAck.getPlus(), out.getDeactivator());
		builder.addConnection(dpAck.getPlus(), out.getDeactivator());
	}
}
