package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.SynchPull;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class SynchPullHandshakes extends HandshakeMaker<SynchPull> {
	@Override
	protected void fillHandshakes(SynchPull component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreateActivePull(component.getWidth()));
		for(int i=0;i<component.getOutputCount();i++)
			handshakes.put("pout"+i, builder.CreatePassivePull(component.getWidth()));
	}
}
