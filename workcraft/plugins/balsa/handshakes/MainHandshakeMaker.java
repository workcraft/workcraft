package org.workcraft.plugins.balsa.handshakes;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.components.Adapt;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class MainHandshakeMaker {
	static Map<Class<? extends Component>, HandshakeMaker<?>> map = getMap();

	public static Map<String, Handshake> getHandshakes(Component component)
	{
		return map.get(component.getClass()).getComponentHandshakes(component);
	}

	private static Map<Class<? extends Component>, HandshakeMaker<?>> getMap() {
		HashMap<Class<? extends Component>, HandshakeMaker<?>> map = new HashMap<Class<? extends Component>, HandshakeMaker<?>>();

		map.put(While.class, new WhileHandshakes());
		map.put(Adapt.class, new AdaptHandshakes());
		map.put(BinaryFunc.class, new BinaryFuncHandshakes());

		return map;
	}
}
