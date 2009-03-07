package org.workcraft.plugins.balsa;

import java.util.Map;

import org.workcraft.dom.Component;
import org.workcraft.dom.VisualComponentGeneratorAttribute;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

@VisualComponentGeneratorAttribute(generator="org.workcraft.plugins.balsa.BreezeVisualComponentGenerator")
public class BreezeComponent extends Component {

	private org.workcraft.plugins.balsa.components.Component underlyingComponent;
	public void setUnderlyingComponent(org.workcraft.plugins.balsa.components.Component underlyingComponent) {
		this.underlyingComponent = underlyingComponent;
	}

	public org.workcraft.plugins.balsa.components.Component getUnderlyingComponent() {
		return underlyingComponent;
	}

	public void setHandshakeComponents(Map<Handshake, HandshakeComponent> handshakes) {
		this.handshakeComponents = handshakes;
	}

	public Map<Handshake, HandshakeComponent> getHandshakeComponents() {
		return handshakeComponents;
	}

	public void setHandshakes(Map<String, Handshake> handshakes) {
		this.handshakes = handshakes;
	}

	public Map<String, Handshake> getHandshakes() {
		return handshakes;
	}

	private Map<Handshake, HandshakeComponent> handshakeComponents;
	private Map<String, Handshake> handshakes;
}
