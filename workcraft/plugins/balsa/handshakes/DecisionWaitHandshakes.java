package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.DecisionWait;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class DecisionWaitHandshakes extends HandshakeMaker<DecisionWait> {
	@Override
	protected void fillHandshakes(DecisionWait component, Map<String, Handshake> handshakes) {
		for(int i=0;i<component.getPortCount();i++)
			handshakes.put("inp"+i, builder.CreatePassiveSync());
		for(int i=0;i<component.getPortCount();i++)
			handshakes.put("out"+i, builder.CreateActiveSync());
		handshakes.put("activate", builder.CreatePassiveSync());
	}
}
