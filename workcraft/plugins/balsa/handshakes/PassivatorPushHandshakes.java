package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.PassivatorPush;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class PassivatorPushHandshakes extends HandshakeMaker<PassivatorPush> {
	@Override
	protected void fillHandshakes(PassivatorPush component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassivePush(component.getWidth()));
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("out"+i, builder.CreatePassivePull(component.getWidth()));
	}
}
