package org.workcraft.plugins.balsa;

import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.ReferenceResolver;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.util.XmlUtil;

public class HandshakeComponent extends Component {
	private BreezeComponent owner;
	private final String handshakeName;

	public HandshakeComponent(Element element, ReferenceResolver referenceResolver)
	{
		super(element);

		initSerialization();

		Element handshakeElement = XmlUtil.getChildElement("Handshake", element);

		handshakeName = handshakeElement.getAttribute("name");

		int ownerId = XmlUtil.readIntAttr(handshakeElement, "owner", -1);

		owner = (BreezeComponent) referenceResolver.getComponentByID(ownerId);
	}

	private void initSerialization() {
		this.addXMLSerialiser(new XMLSerialiser(){
			public String getTagName() {
				return "Handshake";
			}
			public void serialise(Element element) {
				element.setAttribute("name", handshakeName);
				element.setAttribute("owner", owner.getID()+"");
			}
		});
	}

	public HandshakeComponent(BreezeComponent owner, String handshakeName)
	{
		this.owner = owner;
		this.handshakeName = handshakeName;
		initSerialization();
	}

	public BreezeComponent getOwner() {
		return owner;
	}

	public Handshake getHandshake() {
		return owner.getHandshakes().get(handshakeName);
	}

	public Connection getConnection() {
		Set<Connection> connections = getConnections();
		if(connections.size() > 1)
			throw new RuntimeException("Handshake can't have more than 1 connection!");
		if(connections.size() == 0)
			return null;
		return connections.iterator().next();
	}

	public final HandshakeComponent getConnectedHandshake() {
		Connection connection = getConnection();
		if (connection == null)
			return null;
		if (connection.getFirst() == this)
			return (HandshakeComponent) connection.getSecond();
		if (connection.getSecond() == this)
			return (HandshakeComponent) connection.getFirst();
		throw new RuntimeException("Invalid connection");
	}

	public String getHandshakeName() {
		return handshakeName;
	}
}
