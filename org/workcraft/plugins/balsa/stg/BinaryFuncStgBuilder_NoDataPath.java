package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

public class BinaryFuncStgBuilder_NoDataPath extends
		ComponentStgBuilder<BinaryFunc> {

	public BinaryFuncStgBuilder_NoDataPath()
	{
	}

	public void buildStg(BinaryFunc component, Map<String, Process> handshakes, StgBuilder builder) {
		ActivePullStg inpA = (ActivePullStg)handshakes.get("inpA");
		ActivePullStg inpB = (ActivePullStg)handshakes.get("inpB");
		PassivePullStg out = (PassivePullStg)handshakes.get("out");

		//Data path handshaking
		StgSignal dpReq = builder.buildSignal(new SignalId(component, "dpReq"), true);
		StgSignal dpAck = builder.buildSignal(new SignalId(component, "dpAck"), false);
		builder.connect(dpReq.getPlus(), dpAck.getPlus());
		builder.connect(dpAck.getPlus(), dpReq.getMinus());
		builder.connect(dpReq.getMinus(), dpAck.getMinus());
		OutputPlace dpStart = builder.buildPlace(1);
		builder.connect(dpAck.getMinus(), dpStart);
		builder.connect(dpStart, dpReq.getPlus());

		// Read inputs
		builder.connect(out.go(), inpA.go());
		builder.connect(out.go(), inpB.go());

		// When inputs are read, start computing output
		builder.connect(inpA.done(), dpReq.getPlus());
		builder.connect(inpB.done(), dpReq.getPlus());

		// When output is computed, report to the output
		builder.connect(dpAck.getPlus(), out.done());
		builder.connect(dpAck.getPlus(), out.done());

		// When output releases data, release inputs
		builder.connect(out.dataRelease(), inpA.dataRelease());
		builder.connect(out.dataRelease(), inpB.dataRelease());
	}
}
