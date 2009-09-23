package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Fetch;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public class Fetch_NoDataPath extends
		ComponentStgBuilder<Fetch> {

	public Fetch_NoDataPath()
	{
	}

	public void buildStg(Fetch component, Map<String, Process> handshakes, StgBuilder builder) {
		// No data path handshakes needed - direct wires there

		PassiveProcess activate = (PassiveProcess)handshakes.get("activate");
		ActivePullStg in = (ActivePullStg)handshakes.get("inp");
		ActivePushStg out = (ActivePushStg)handshakes.get("out");

		//First, make sure input sets up the correct data
		builder.connect(activate.go(), in.go());
		//After it does, activate output
		builder.connect(in.done(), out.go());
		//After out acknowledges, RTZ input and acknowledge back
		builder.connect(out.done(), in.dataRelease());
		builder.connect(out.done(), activate.done());
	}
}
