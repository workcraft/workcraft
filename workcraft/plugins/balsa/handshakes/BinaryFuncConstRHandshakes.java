package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFuncConstR;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class BinaryFuncConstRHandshakes extends HandshakeMaker<BinaryFuncConstR> {

	@Override
	protected void fillHandshakes(BinaryFuncConstR component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreateActivePull(component.getInputWidth()));
		handshakes.put("out", builder.CreatePassivePull(component.getOutputWidth()));
	}

}
