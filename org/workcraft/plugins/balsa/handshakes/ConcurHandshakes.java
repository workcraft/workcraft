package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class ConcurHandshakes extends HandshakeMaker<Concur> {

	@Override
	protected void fillHandshakes(Concur component,	Map<String, Handshake> handshakes) {
		handshakes.put("activate", builder.CreatePassiveSync());
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("activateOut"+i, builder.CreateActiveSync());
	}

}
