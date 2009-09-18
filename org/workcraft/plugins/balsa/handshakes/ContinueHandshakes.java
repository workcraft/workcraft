package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Continue;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class ContinueHandshakes extends HandshakeMaker<Continue> {
	@Override
	protected void fillHandshakes(Continue component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassiveSync());
	}
}
