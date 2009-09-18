package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Synch;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class SynchHandshakes extends HandshakeMaker<Synch> {
	@Override
	protected void fillHandshakes(Synch component, Map<String, Handshake> handshakes) {
		for(int i=0;i<component.getInputCount();i++)
			handshakes.put("inp"+i, builder.CreatePassiveSync());
		handshakes.put("out", builder.CreateActiveSync());
	}
}
