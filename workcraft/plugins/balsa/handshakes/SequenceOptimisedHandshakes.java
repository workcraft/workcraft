package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.SequenceOptimised;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class SequenceOptimisedHandshakes extends HandshakeMaker<SequenceOptimised> {

	@Override
	protected void fillHandshakes(SequenceOptimised component, Map<String, Handshake> handshakes) {
		handshakes.put("activate", builder.CreatePassiveSync());
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("activateOut"+i, builder.CreateActiveSync());
	}

}
