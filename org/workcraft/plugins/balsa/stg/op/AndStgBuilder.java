package org.workcraft.plugins.balsa.stg.op;

import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stg.ComponentStgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public class AndStgBuilder extends ComponentStgBuilder<BinaryFunc> {

	public void buildStg(BinaryFunc component, Map<String, Process> handshakes, StgBuilder builder) {
		throw new RuntimeException("Not implemented");
/*		ActivePull inpA = (ActivePull)handshakes.get("inpA");
		ActivePull inpB = (ActivePull)handshakes.get("inpB");
		PassivePull out = (PassivePull)handshakes.get("out");*/
	}

}
