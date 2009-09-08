package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.CallMux;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;

public class CallMux_NoDataPath extends
		ComponentStgBuilder<CallMux> {

	public CallMux_NoDataPath()
	{
	}

	public void buildStg(CallMux component, Map<String, StgHandshake> handshakes, StgBuilder builder) {
		ActivePushStg out = (ActivePushStg)handshakes.get("out");

		StgSignal selAc = builder.buildSignal(new SignalId(component, "selAq"), false);
		StgPlace selRequested = builder.buildPlace();
		StgPlace selRtz = builder.buildPlace();
		StgPlace selAcknowledged = builder.buildPlace();
		StgPlace selAckReset = builder.buildPlace(1);
		builder.addConnection(selAc.getPlus(), selAcknowledged);
		builder.addConnection(selAc.getMinus(), selAckReset);

		builder.addConnection(selRequested, selAc.getPlus());
		builder.addConnection(selRtz, selAc.getMinus());

		StgPlace releaseInput = builder.buildPlace();
		StgPlace releaseSel = builder.buildPlace();
		builder.addConnection(out.getDeactivate(), releaseInput);
		builder.addConnection(out.getDeactivate(), releaseSel);

		StgPlace ready = builder.buildPlace(1);

		for(int i=0;i<component.getInputCount();i++)
		{
			PassivePushStg in = (PassivePushStg)handshakes.get("inp"+i);

			//TODO! Move environment specification somewhere else
			builder.addConnection(ready, (StgTransition)in.getActivate());
			builder.addConnection(in.getDeactivate(), ready);

			StgSignal selRq = builder.buildSignal(new SignalId(component, "sel"+i+"Rq"), true);
			StgPlace selReady = builder.buildPlace(1);
			StgPlace selActive = builder.buildPlace();
			builder.addConnection(selReady, selRq.getPlus());
			builder.addConnection(selRq.getPlus(), selActive);
			builder.addConnection(selActive, selRq.getMinus());
			builder.addConnection(selRq.getMinus(), selReady);

			builder.addConnection(selRq.getPlus(), selRequested);
			builder.addConnection(selAcknowledged, selRq.getMinus());
			builder.addConnection(selRq.getMinus(), selRtz);
			builder.addConnection(selAckReset, selRq.getPlus());

			builder.addConnection(in.getActivate(), selRq.getPlus());
			builder.addConnection(releaseSel, selRq.getMinus());
			builder.addConnection(releaseInput, in.getDataReleased());
		}

		builder.addConnection(selAc.getPlus(), out.getActivate());
	}
}
