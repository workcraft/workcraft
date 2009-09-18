package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Call;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class CallHandshakes extends HandshakeMaker<Call> {

	@Override
	protected void fillHandshakes(Call component, Map<String, Handshake> handshakes) {
		handshakes.put("out", builder.CreateActiveSync());
		for(int i=0;i<component.getInputCount();i++)
			handshakes.put("inp"+i, builder.CreatePassiveSync());
	}

}
