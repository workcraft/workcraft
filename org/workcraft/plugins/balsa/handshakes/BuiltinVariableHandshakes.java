package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.BuiltinVariable;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class BuiltinVariableHandshakes extends HandshakeMaker<BuiltinVariable> {

	@Override
	protected void fillHandshakes(BuiltinVariable component, Map<String, Handshake> handshakes) {
		handshakes.put("write", builder.CreatePassivePush(64));
		for(int i=0;i<component.getReadPortCount();i++)
			handshakes.put("read"+i, builder.CreatePassivePull(64));
	}

}
