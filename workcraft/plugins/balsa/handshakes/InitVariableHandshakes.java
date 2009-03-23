package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.InitVariable;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class InitVariableHandshakes extends HandshakeMaker<InitVariable> {
	@Override
	protected void fillHandshakes(InitVariable component, Map<String, Handshake> handshakes) {
		handshakes.put("write", builder.CreatePassivePush(component.getWidth()));
		for(int i=0;i<component.getReadPortCount();i++)
			handshakes.put("read"+i, builder.CreatePassivePull(component.getWidth()));
	}
}
