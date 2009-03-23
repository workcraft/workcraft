package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.CallMux;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class CallMuxHandshakes extends HandshakeMaker<CallMux> {

	@Override
	protected void fillHandshakes(CallMux component, Map<String, Handshake> handshakes) {
		int width = component.getWidth();
		handshakes.put("out", builder.CreateActivePush(width));
		for(int i=0;i<component.getInputCount();i++)
			handshakes.put("inp"+i, builder.CreatePassivePush(width));
	}

}
