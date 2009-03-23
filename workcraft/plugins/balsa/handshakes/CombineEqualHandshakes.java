package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.CombineEqual;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class CombineEqualHandshakes extends HandshakeMaker<CombineEqual> {

	@Override
	protected void fillHandshakes(CombineEqual component, Map<String, Handshake> handshakes) {
		handshakes.put("out", builder.CreatePassivePull(component.getOutputWidth()));
		for(int i=0;i<component.getInputCount();i++)
			handshakes.put("inp"+i, builder.CreateActivePull(component.getInputWidth()));
	}

}
