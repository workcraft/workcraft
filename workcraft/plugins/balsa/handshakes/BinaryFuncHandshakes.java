package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class BinaryFuncHandshakes extends HandshakeMaker<BinaryFunc> {

	@Override
	protected void fillHandshakes(BinaryFunc component, Map<String, Handshake> handshakes) {
		handshakes.put("inpA", builder.CreateActivePull(component.getInputAWidth()));
		handshakes.put("inpB", builder.CreateActivePull(component.getInputAWidth()));
		handshakes.put("out", builder.CreatePassivePull(component.getOutputWidth()));
	}

}
