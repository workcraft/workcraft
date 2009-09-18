package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Fetch;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class FetchHandshakes extends HandshakeMaker<Fetch> {
	@Override
	protected void fillHandshakes(Fetch component, Map<String, Handshake> handshakes) {
		handshakes.put("activate", builder.CreatePassiveSync());
		handshakes.put("inp", builder.CreateActivePull(component.getWidth()));
		handshakes.put("out", builder.CreateActivePush(component.getWidth()));
	}
}
