package org.workcraft.plugins.balsa.handshakes;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.HandshakeBuilder;
import org.workcraft.plugins.balsa.handshakebuilder.SimpleHandshakeBuilder;

public abstract class HandshakeMaker<T> {

	protected static HandshakeBuilder builder = SimpleHandshakeBuilder.getInstance();

	@SuppressWarnings("unchecked")
	public Map<String, Handshake> getComponentHandshakes(Component component) {
		HashMap<String, Handshake> map = new HashMap<String, Handshake>();
		fillHandshakes((T)component, map);
		return map;
	}

	abstract protected void fillHandshakes(T component, Map<String, Handshake> handshakes);
}
