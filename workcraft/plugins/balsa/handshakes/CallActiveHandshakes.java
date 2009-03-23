package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.CallActive;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class CallActiveHandshakes extends HandshakeMaker<CallActive> {

	@Override
	protected void fillHandshakes(CallActive component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassiveSync());
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("out"+i, builder.CreateActiveSync());
	}

}
