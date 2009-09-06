package org.workcraft.plugins.balsa;

import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class HandshakeComponent extends MathNode {
	private BreezeComponent owner;
	private String handshakeName;

	public HandshakeComponent(BreezeComponent owner, String handshakeName)
	{
		this.owner = owner;
		this.handshakeName = handshakeName;
	}

	public BreezeComponent getOwner() {
		return owner;
	}

	public Handshake getHandshake() {
		return owner.getHandshakes().get(handshakeName);
	}

	public String getHandshakeName() {
		return handshakeName;
	}
}
