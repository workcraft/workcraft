package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.ContinuePush;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class ContinuePushHandshakes extends HandshakeMaker<ContinuePush> {
	@Override
	protected void fillHandshakes(ContinuePush component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassivePush(component.getWidth()));
	}
}
