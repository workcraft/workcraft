package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Slice;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class SliceHandshakes extends HandshakeMaker<Slice> {
	@Override
	protected void fillHandshakes(Slice component, Map<String, Handshake> handshakes) {
		handshakes.put("inp", builder.CreateActivePull(component.getInputWidth()));
		handshakes.put("out", builder.CreatePassivePull(component.getOutputWidth()));
	}
}
