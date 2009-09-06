package org.workcraft.plugins.balsa;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchyObserver;
import org.workcraft.framework.observation.NodesAddedEvent;
import org.workcraft.plugins.balsa.handshakebuilder.ActivePull;
import org.workcraft.plugins.balsa.handshakebuilder.ActivePush;
import org.workcraft.plugins.balsa.handshakebuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakebuilder.DataHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.PassivePull;
import org.workcraft.plugins.balsa.handshakebuilder.PassivePush;
import org.workcraft.plugins.balsa.handshakebuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakes.MainHandshakeMaker;

@VisualClass ("org.workcraft.plugins.balsa.VisualBalsaCircuit")
@DisplayName ("Balsa circuit")

public final class BalsaCircuit extends AbstractMathModel {

	public BalsaCircuit() {
		super(null);

		((MathGroup)getRoot()).addObserver(new HierarchyObserver(){
			@Override
			public void notify(HierarchyEvent e) {
				if(e instanceof NodesAddedEvent)
					for(Node node : e.getAffectedNodes())
					{
						if(node instanceof BreezeComponent)
							createHandshakeComponents((BreezeComponent)node);
						if(node instanceof HandshakeComponent)
							handshakeAdded((HandshakeComponent)node);
					}
				// TODO: delete handshake components if needed
				// TODO: re-create handshakes if needed
			}
		});
	}

	private void handshakeAdded(HandshakeComponent component) {
		Map<Handshake, HandshakeComponent> handshakeComponents = component.getOwner().getHandshakeComponents();
		if(handshakeComponents == null)
			return;
		Handshake handshake = component.getHandshake();
		HandshakeComponent existing = handshakeComponents.get(handshake);
		if(existing == component)
			return;

		remove(existing);

		handshakeComponents.put(handshake, component);

		component.getOwner().setHandshakeComponents(handshakeComponents);
	}

	private void createHandshakeComponents(BreezeComponent component) {
		HashMap<Handshake, HandshakeComponent> handshakeComponents = new HashMap<Handshake, HandshakeComponent>();
		Map<String, Handshake> handshakes = MainHandshakeMaker.getHandshakes(component.getUnderlyingComponent());
		for(String handshakeName : handshakes.keySet())
		{
			Handshake handshake = handshakes.get(handshakeName);
			HandshakeComponent hcomp = new HandshakeComponent(component, handshakeName);
			handshakeComponents.put(handshake, hcomp);
			add(hcomp);
		}
		component.setHandshakeComponents(handshakeComponents);
		component.setHandshakes(handshakes);
	}

	@Override
	public void validate() throws ModelValidationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void validateConnection(Node first, Node second)
		throws InvalidConnectionException {
		if(!(first instanceof HandshakeComponent && second instanceof HandshakeComponent))
			throw new InvalidConnectionException("Only handshakes can be connected");
		validateConnection((HandshakeComponent)first, (HandshakeComponent)second);
	}

	public void validateConnection(HandshakeComponent first, HandshakeComponent second)
			throws InvalidConnectionException {

		if(getPostset(first).size() > 0 || getPreset(first).size() > 0 ||
				getPostset(second).size() > 0 || getPreset(second).size() > 0)
			throw new InvalidConnectionException("Cannot connect already connected handshakes");

		Handshake h1 = first.getHandshake();
		Handshake h2 = second.getHandshake();

		if(!(h1 instanceof ActiveSync && h2 instanceof PassiveSync ||
			h2 instanceof ActiveSync && h1 instanceof PassiveSync))
			throw new InvalidConnectionException("Must connect passive and active handshakes");

		boolean isData1 = h1 instanceof DataHandshake;
		boolean isData2 = h2 instanceof DataHandshake;
		if(isData1 != isData2)
			throw new InvalidConnectionException("Cannot connect data handshake with an activation handshake");

		if(isData1)
		{
			DataHandshake dh1 = (DataHandshake)h1;
			DataHandshake dh2 = (DataHandshake)h2;

			boolean push1 = isPush(dh1);
			boolean push2 = isPush(dh2);

			if(push1 != push2)
				throw new InvalidConnectionException("Cannot connect push handshake with pull handshake");

			if(dh1.getWidth() != dh2.getWidth())
				throw new InvalidConnectionException("Cannot connect data handshakes with different bit widths");
		}
	}

	private boolean isPush(DataHandshake handshake)
	{
		if (handshake instanceof ActivePush || handshake instanceof PassivePush)
			return true;
		if (handshake instanceof ActivePull || handshake instanceof PassivePull)
			return false;
		throw new RuntimeException("Unknown data handshake type");
	}

	public Connection getConnection(HandshakeComponent handshake) {
		Set<Connection> connections = getConnections(handshake);
		if(connections.size() > 1)
			throw new RuntimeException("Handshake can't have more than 1 connection!");
		if(connections.size() == 0)
			return null;
		return connections.iterator().next();
	}

	public final HandshakeComponent getConnectedHandshake(HandshakeComponent handshake) {
		Connection connection = getConnection(handshake);
		if (connection == null)
			return null;
		if (connection.getFirst() == handshake)
			return (HandshakeComponent) connection.getSecond();
		if (connection.getSecond() == handshake)
			return (HandshakeComponent) connection.getFirst();
		throw new RuntimeException("Invalid connection");
	}
}
