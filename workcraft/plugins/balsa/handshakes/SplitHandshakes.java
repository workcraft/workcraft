package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Split;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class SplitHandshakes extends HandshakeMaker<Split> {
	@Override
	protected void fillHandshakes(Split component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassivePush(component.getInputWidth()));
		handshakes.put("LSOut", builder.CreateActivePush(component.getLsOutputWidth()));
		handshakes.put("MSOut", builder.CreateActivePush(component.getMsOutputWidth()));
	}
}
