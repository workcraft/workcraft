package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.NullAdapt;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class NullAdaptHandshakes extends HandshakeMaker<NullAdapt> {
	@Override
	protected void fillHandshakes(NullAdapt component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreatePassivePush(component.getInputWidth()));
		handshakes.put("out", builder.CreateActiveSync());
	}
}
