package org.workcraft.plugins.balsa;

import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
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

	public Connection getConnection() {
		Set<Connection> connections = getConnections();
		if(connections.size() > 1)
			throw new RuntimeException("Handshake can't have more than 1 connection!");
		if(connections.size() == 0)
			return null;
		return connections.iterator().next();
	}
}
