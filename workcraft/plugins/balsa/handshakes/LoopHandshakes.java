package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Loop;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class LoopHandshakes extends HandshakeMaker<Loop> {
	@Override
	protected void fillHandshakes(Loop component, Map<String, Handshake> handshakes) {
		handshakes.put("activate", builder.CreatePassiveSync());
		handshakes.put("activateOut", builder.CreateActiveSync());
	}
}
