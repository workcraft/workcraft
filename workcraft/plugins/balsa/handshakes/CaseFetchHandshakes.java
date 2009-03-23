package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.CaseFetch;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class CaseFetchHandshakes extends HandshakeMaker<CaseFetch> {

	@Override
	protected void fillHandshakes(CaseFetch component, Map<String, Handshake> handshakes) {
		handshakes.put("index", builder.CreateActivePull(component.getIndexWidth()));
		int width = component.getWidth();
		for(int i=0;i<component.getInputCount();i++)
			handshakes.put("inp"+i, builder.CreateActivePull(width));
		handshakes.put("out", builder.CreatePassivePull(width));
	}

}
