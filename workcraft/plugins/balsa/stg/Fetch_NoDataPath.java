package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Fetch;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public class Fetch_NoDataPath extends
		ComponentStgBuilder<Fetch> {

	public Fetch_NoDataPath()
	{
	}

	public void buildStg(Fetch component, Map<String, StgHandshake> handshakes, StgBuilder builder) {
		// No data path handshakes needed - direct wires there

		PassiveSyncStg activate = (PassiveSyncStg)handshakes.get("activate");
		ActivePullStg in = (ActivePullStg)handshakes.get("inp");
		ActivePushStg out = (ActivePushStg)handshakes.get("out");

		//First, make sure input sets up the correct data
		builder.addConnection(activate.getActivate(), in.getActivate());
		//After it does, activate output
		builder.addConnection(in.getDataReady(), out.getActivate());
		//After out acknowledges, RTZ input and acknowledge back
		builder.addConnection(out.getDeactivate(), in.getDataRelease());
		builder.addConnection(out.getDeactivate(), activate.getDeactivate());
	}
}
