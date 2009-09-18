package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.PassiveEagerFalseVariable;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class PassiveEagerFalseVariableHandshakes extends HandshakeMaker<PassiveEagerFalseVariable> {
	@Override
	protected void fillHandshakes(PassiveEagerFalseVariable component, Map<String, Handshake> handshakes) {
		handshakes.put("activate", builder.CreatePassiveSync());
		handshakes.put("signal", builder.CreateActiveSync());
		handshakes.put("write", builder.CreatePassivePush(component.getWidth()));
		for(int i=0;i<component.getReadPortCount();i++)
			handshakes.put("read"+i, builder.CreatePassivePull(component.getWidth())); // TODO : find out how to use the getSpecification
	}
}
