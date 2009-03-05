package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.components.Adapt;

public class AdaptHandshakes extends HandshakeMaker<Adapt> {

	@Override
	public void fillHandshakes(Adapt component, Map<String, Handshake> map) {
		map.put("inp", builder.CreateActivePull(component.getInputWidth()));
		map.put("out", builder.CreatePassivePull(component.getOutputWidth()));
	}

}
