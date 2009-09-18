package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.CallDemux;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class CallDemuxHandshakes extends HandshakeMaker<CallDemux> {

	@Override
	protected void fillHandshakes(CallDemux component, Map<String, Handshake> handshakes) {
		int width = component.getWidth();
		handshakes.put("inp", builder.CreateActivePull(width));
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("out"+i, builder.CreatePassivePull(width));
	}

}
