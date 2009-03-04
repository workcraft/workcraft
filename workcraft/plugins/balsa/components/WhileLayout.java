package org.workcraft.plugins.balsa.components;

import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class WhileLayout implements HandshakeComponentLayout{
	private final While component;

	public WhileLayout(While component)
	{
		this.component = component;
	}

	@Override
	public Handshake getBottom() {
		return null;
	}

	@Override
	public Handshake[][] getLeft() {
		return new Handshake[][]{new Handshake[]{component.getHandshakes().get("activate")}};
	}

	@Override
	public Handshake[][] getRight() {
		return new Handshake[][]{new Handshake[]{component.getHandshakes().get("activateOut")}};
	}

	@Override
	public Handshake getTop() {
		return component.getHandshakes().get("activate");
	}
}
