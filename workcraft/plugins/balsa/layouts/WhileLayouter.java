package org.workcraft.plugins.balsa.layouts;

import java.util.Map;

import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class WhileLayouter extends Layouter<While>{
	public HandshakeComponentLayout getLayout(While component, final Map<String, Handshake> handshakes)
	{
		return new HandshakeComponentLayout()
		{
			public Handshake getBottom() {
				return null;
			}

			public Handshake[][] getLeft() {
				return new Handshake[][]{new Handshake[]{handshakes.get("guard")}};
			}

			public Handshake[][] getRight() {
				return new Handshake[][]{new Handshake[]{handshakes.get("activateOut")}};
			}

			public Handshake getTop() {
				return handshakes.get("activate");
			}
		};
	}
}
