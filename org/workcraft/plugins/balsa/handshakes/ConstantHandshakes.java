package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Constant;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class ConstantHandshakes extends HandshakeMaker<Constant> {
	@Override
	protected void fillHandshakes(Constant component, Map<String, Handshake> handshakes) {
		handshakes.put("out", builder.CreatePassivePull(component.getWidth()));
	}
}
