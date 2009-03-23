package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Encode;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class EncodeHandshakes extends HandshakeMaker<Encode> {
	@Override
	protected void fillHandshakes(Encode component, Map<String, Handshake> handshakes) {
		for(int i=0;i<component.getInputCount();i++)
			handshakes.put("inp"+i, builder.CreatePassiveSync());
		handshakes.put("out", builder.CreateActivePush(component.getOutputWidth()));
	}
}
