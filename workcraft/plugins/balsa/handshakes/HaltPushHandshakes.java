package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.HaltPush;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class HaltPushHandshakes extends HandshakeMaker<HaltPush> {
	@Override
	protected void fillHandshakes(HaltPush component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassivePush(component.getWidth()));
	}
}
