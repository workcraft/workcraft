package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.SplitEqual;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class SplitEqualHandshakes extends HandshakeMaker<SplitEqual> {
	@Override
	protected void fillHandshakes(SplitEqual component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassivePush(component.getInputWidth()));
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("out"+i, builder.CreateActivePush(component.getOutputWidth()));
	}
}
