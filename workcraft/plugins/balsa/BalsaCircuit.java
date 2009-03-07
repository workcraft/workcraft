package org.workcraft.plugins.balsa;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakes.MainHandshakeMaker;

@VisualClass ("org.workcraft.plugins.balsa.VisualBalsaCircuit")
@DisplayName ("Balsa circuit")
public final class BalsaCircuit extends MathModel {

	public BalsaCircuit() {
		super();
		addComponentSupport(WhileComponent.class);
		addComponentSupport(AdaptComponent.class);
		addComponentSupport(BinaryFuncComponent.class);

		this.addListener(new MathModelListener(){

			public void onComponentAdded(Component component) {
				if(component instanceof BreezeComponent)
					createHandshakeComponents((BreezeComponent)component);
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

	private void createHandshakeComponents(BreezeComponent component) {
		HashMap<Handshake, HandshakeComponent> handshakeComponents = new HashMap<Handshake, HandshakeComponent>();
		Map<String, Handshake> handshakes = MainHandshakeMaker.getHandshakes(component.getUnderlyingComponent());
		for(Handshake handshake : handshakes.values())
		{
			HandshakeComponent hcomp = new HandshakeComponent(component, handshake);
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
		// TODO Auto-generated method stub
	}
}
