package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.PassiveSyncEagerFalseVariable;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class PassiveSyncEagerFalseVariableHandshakes extends HandshakeMaker<PassiveSyncEagerFalseVariable> {
	@Override
	protected void fillHandshakes(PassiveSyncEagerFalseVariable component, Map<String, Handshake> handshakes) {
		handshakes.put("activate", builder.CreatePassiveSync());
		handshakes.put("write", builder.CreatePassiveSync());
		handshakes.put("activateOut", builder.CreateActiveSync());
	}
}
