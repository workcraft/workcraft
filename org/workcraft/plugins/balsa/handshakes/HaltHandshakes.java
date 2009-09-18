package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Halt;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class HaltHandshakes extends HandshakeMaker<Halt> {
	@Override
	protected void fillHandshakes(Halt component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassiveSync());
	}
}
