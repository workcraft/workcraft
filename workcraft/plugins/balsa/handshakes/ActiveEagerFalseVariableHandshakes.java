package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.ActiveEagerFalseVariable;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

class ActiveEagerFalseVariableHandshakes extends HandshakeMaker<ActiveEagerFalseVariable>
{
	@Override
	protected void fillHandshakes(ActiveEagerFalseVariable component, Map<String, Handshake> handshakes) {
		handshakes.put("activate", builder.CreatePassiveSync());
		handshakes.put("write", builder.CreateActivePull(component.getWidth()));
		handshakes.put("signal", builder.CreateActiveSync());
		for(int i=0;i<component.getReadPortCount();i++)
			handshakes.put("read"+i, builder.CreatePassivePull(8));//TODO: find out how the "specification" should be used
	}
}
