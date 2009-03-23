package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.FalseVariable;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class FalseVariableHandshakes extends HandshakeMaker<FalseVariable> {
	@Override
	protected void fillHandshakes(FalseVariable component, Map<String, Handshake> handshakes) {
		handshakes.put("write", builder.CreatePassivePush(component.getWidth()));
		handshakes.put("signal", builder.CreateActiveSync());
		for(int i=0;i<component.getReadPortCount();i++)
			handshakes.put("read"+i, builder.CreatePassivePull(component.getWidth())); // TODO : find out how to use the getSpecification
	}
}
