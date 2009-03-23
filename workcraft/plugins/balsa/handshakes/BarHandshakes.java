package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Bar;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

class BarHandshakes extends HandshakeMaker<Bar> {

	@Override
	protected void fillHandshakes(Bar component, Map<String, Handshake> handshakes) {
		for(int i=0;i<component.getGuardCount();i++)
		{
			handshakes.put("guard"+i, builder.CreateActivePull(1));
			handshakes.put("activateOut"+i, builder.CreateActiveSync());
		}
		handshakes.put("guard", builder.CreatePassivePull(1));
		handshakes.put("activate", builder.CreatePassiveSync());
	}

}
