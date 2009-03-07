package org.workcraft.plugins.balsa;

import org.workcraft.dom.Component;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class HandshakeComponent extends Component {
	private final BreezeComponent owner;
	private final Handshake handshake;

	public HandshakeComponent(BreezeComponent owner, Handshake handshake)
	{
		this.owner = owner;
		this.handshake = handshake;
	}

	public BreezeComponent getOwner() {
		return owner;
	}

	public Handshake getHandshake() {
		return handshake;
	}
}
