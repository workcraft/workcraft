package org.workcraft.plugins.balsa.components;

import java.util.Map;

import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.HandshakeBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public interface BalsaComponentDefinition {
	public Map<String, Handshake> createHandshakes(HandshakeBuilder builder);
	public void buildStg(Map<String, StgHandshake> handshakes, StgBuilder builder);
}
