package org.workcraft.plugins.balsa.layouts;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class ConcurLayouter extends Layouter<Concur> {

	@Override
	HandshakeComponentLayout getLayout(final Concur component, final Map<String, Handshake> handshakes) {
		return new HandshakeComponentLayout()
		{
			public Handshake getBottom() {
				return null;
			}
			public Handshake getTop() {
				return null;
			}
			public Handshake[][] getRight() {
				return new Handshake[][]{getPortArray(handshakes, "activateOut", component.getOutputCount())};
			}
			public Handshake[][] getLeft() {
				return new Handshake[][]{new Handshake[]{handshakes.get("activate")}};
			}
		};
	}

}
