package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Arbiter;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

class ArbiterHandshakes extends HandshakeMaker<Arbiter> {

	@Override
	protected void fillHandshakes(Arbiter component, Map<String, Handshake> handshakes) {
		handshakes.put("inpA", builder.CreatePassiveSync());
		handshakes.put("inpB", builder.CreatePassiveSync());
		handshakes.put("outA", builder.CreateActiveSync());
		handshakes.put("outB", builder.CreateActiveSync());
	}

}
