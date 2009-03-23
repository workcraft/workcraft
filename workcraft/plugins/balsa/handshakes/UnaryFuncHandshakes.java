package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.UnaryFunc;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class UnaryFuncHandshakes extends HandshakeMaker<UnaryFunc> {
	@Override
	protected void fillHandshakes(UnaryFunc component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreateActivePull(component.getInputWidth()));
		handshakes.put("out", builder.CreatePassivePull(component.getOutputWidth()));
	}
}
