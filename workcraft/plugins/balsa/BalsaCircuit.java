package org.workcraft.plugins.balsa;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.AbstractMathModel;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
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
		super();
		addComponentSupport(BreezeComponent.class);
		addComponentSupport(HandshakeComponent.class);

		this.addListener(new MathModelListener(){

			public void onComponentAdded(Component component) {
				if(component instanceof BreezeComponent)
					createHandshakeComponents((BreezeComponent)component);
				if(component instanceof HandshakeComponent)
					handshakeAdded((HandshakeComponent)component);
			}

			public void onComponentRemoved(Component component) {
				// TODO: delete handshake components
			}

			public void onConnectionAdded(Connection connection) {
			}

			public void onConnectionRemoved(Connection connection) {
			}

			public void onNodePropertyChanged(String propertyName, MathNode n) {
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

		removeComponent(existing);

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
			addComponent(hcomp);
		}
		component.setHandshakeComponents(handshakeComponents);
		component.setHandshakes(handshakes);
	}

	@Override
	public void validate() throws ModelValidationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void validateConnection(Connection connection)
			throws InvalidConnectionException {
		HandshakeComponent first = (HandshakeComponent)connection.getFirst();
		HandshakeComponent second = (HandshakeComponent)connection.getSecond();

		if(first.getPostset().size() + first.getPreset().size() +
			second.getPostset().size() + second.getPreset().size() > 0)
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
}
