package org.workcraft.plugins.balsa.layouts;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Adapt;
import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class AdaptLayouter extends Layouter<Adapt> {

	@Override
	public HandshakeComponentLayout getLayout(Adapt component, final Map<String, Handshake> handshakes) {
		return new HandshakeComponentLayout()
		{
			public Handshake getBottom() {
				return null;
			}
			public Handshake getTop() {
				return null;
			}
			public Handshake[][] getLeft() {
				return new Handshake[][]{new Handshake[]{handshakes.get("inp")}};
			}
			public Handshake[][] getRight() {
				return new Handshake[][]{new Handshake[]{handshakes.get("out")}};
			}
		};
	}

}
