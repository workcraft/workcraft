package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Fork;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class ForkHandshakes extends HandshakeMaker<Fork> {
	@Override
	protected void fillHandshakes(Fork component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassiveSync());
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("out"+i, builder.CreateActiveSync());
	}
}
