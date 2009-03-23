package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Case;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class CaseHandshakes extends HandshakeMaker<Case> {

	@Override
	protected void fillHandshakes(Case component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreateActivePull(component.getInputWidth()));
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("activateOut"+i, builder.CreatePassiveSync());
	}

}
