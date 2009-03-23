package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.ForkPush;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class ForkPushHandshakes extends HandshakeMaker<ForkPush> {
	@Override
	protected void fillHandshakes(ForkPush component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassivePush(component.getWidth()));
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("out"+i, builder.CreateActivePush(component.getWidth()));
	}
}
