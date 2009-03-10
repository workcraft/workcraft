package org.workcraft.plugins.balsa;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class HandshakeComponent extends Component {
	private BreezeComponent owner;
	private Handshake handshake;

	public HandshakeComponent(Element element)
	{
		super(element);
	}

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
