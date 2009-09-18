package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.SynchPush;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class SynchPushHandshakes extends HandshakeMaker<SynchPush> {
	@Override
	protected void fillHandshakes(SynchPush component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassivePush(component.getWidth()));
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("pout"+i, builder.CreatePassivePull(component.getWidth()));
		handshakes.put("aout", builder.CreateActivePush(component.getWidth()));
	}
}
