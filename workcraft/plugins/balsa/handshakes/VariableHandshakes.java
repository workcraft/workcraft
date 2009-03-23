package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Variable;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class VariableHandshakes extends HandshakeMaker<Variable> {
	@Override
	protected void fillHandshakes(Variable component, Map<String, Handshake> handshakes) {
		handshakes.put("write", builder.CreatePassivePush(component.getWidth()));
		for(int i=0;i<component.getReadPortCount();i++)
			handshakes.put("read"+i, builder.CreatePassivePull(component.getWidth())); // TODO : find out how to use the getSpecification
	}
}
