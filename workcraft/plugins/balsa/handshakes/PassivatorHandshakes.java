package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Passivator;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class PassivatorHandshakes extends HandshakeMaker<Passivator> {
	@Override
	protected void fillHandshakes(Passivator component, Map<String, Handshake> handshakes) {
		for(int i=0;i<component.getCount();i++)
			handshakes.put("inp"+i, builder.CreatePassiveSync());
	}
}
