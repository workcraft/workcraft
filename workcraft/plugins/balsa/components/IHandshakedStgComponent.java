package org.workcraft.plugins.balsa.components;

import java.util.Map;

import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;

public interface IHandshakedStgComponent {
	public void buildStg(HandshakeStgBuilder builder);
	public Map<String, Handshake> getHandshakes();
}
