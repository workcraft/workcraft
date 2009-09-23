package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.CallMux;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;

public class CallMux_NoDataPath extends
		ComponentStgBuilder<CallMux> {

	public CallMux_NoDataPath()
	{
	}

	public void buildStg(CallMux component, Map<String, Process> handshakes, StgBuilder builder) {
		ActivePushStg out = (ActivePushStg)handshakes.get("out");

		StgSignal selAc = builder.buildSignal(new SignalId(component, "selAq"), false);
		OutputPlace selRequested = builder.buildPlace();
		OutputPlace selRtz = builder.buildPlace();
		OutputPlace selAcknowledged = builder.buildPlace();
		OutputPlace selAckReset = builder.buildPlace(1);
		builder.connect(selAc.getPlus(), selAcknowledged);
		builder.connect(selAc.getMinus(), selAckReset);

		builder.connect(selRequested, selAc.getPlus());
		builder.connect(selRtz, selAc.getMinus());

		OutputPlace releaseInput = builder.buildPlace();
		OutputPlace releaseSel = builder.buildPlace();
		builder.connect(out.done(), releaseInput);
		builder.connect(out.done(), releaseSel);

		OutputPlace ready = builder.buildPlace(1);

		for(int i=0;i<component.getInputCount();i++)
		{
			PassivePushStg in = (PassivePushStg)handshakes.get("inp"+i);

			//TODO! Move environment specification somewhere else
			builder.connect(ready, (OutputEvent)in.go());
			builder.connect(in.done(), ready);

			StgSignal selRq = builder.buildSignal(new SignalId(component, "sel"+i+"Rq"), true);
			OutputPlace selReady = builder.buildPlace(1);
			OutputPlace selActive = builder.buildPlace();
			builder.connect(selReady, selRq.getPlus());
			builder.connect(selRq.getPlus(), selActive);
			builder.connect(selActive, selRq.getMinus());
			builder.connect(selRq.getMinus(), selReady);

			builder.connect(selRq.getPlus(), selRequested);
			builder.connect(selAcknowledged, selRq.getMinus());
			builder.connect(selRq.getMinus(), selRtz);
			builder.connect(selAckReset, selRq.getPlus());

			builder.connect(in.go(), selRq.getPlus());
			builder.connect(releaseSel, selRq.getMinus());
			builder.connect(releaseInput, in.dataRelease());
		}

		builder.connect(selAc.getPlus(), out.go());
	}
}
