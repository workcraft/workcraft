package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Combine;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class CombineHandshakes extends HandshakeMaker<Combine> {

	@Override
	protected void fillHandshakes(Combine component, Map<String, Handshake> handshakes) {
		handshakes.put("out", builder.CreatePassivePull(component.getOutputWidth()));
		handshakes.put("LSInp", builder.CreateActivePull(component.getLSInputWidth()));
		handshakes.put("MSInp", builder.CreateActivePull(component.getMSInputWidth()));
	}

}
