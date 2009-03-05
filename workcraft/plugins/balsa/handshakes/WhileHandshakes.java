package org.workcraft.plugins.balsa.handshakes;

import java.util.Map;

import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class WhileHandshakes extends HandshakeMaker<While> {
	public void fillHandshakes(While component, Map<String, Handshake> map)
	{
		map.put("guard", builder.CreateActivePull(1));
		map.put("activate", builder.CreatePassiveSync());
		map.put("activateOut", builder.CreateActiveSync());
	}
}
