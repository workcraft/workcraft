package org.workcraft.plugins.balsa.layouts;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.Adapt;
import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class MainLayouter {

	static Map<Class<? extends Component>, Layouter<?>> map = getMap();

	public static HandshakeComponentLayout getLayout(Component component, Map<String, Handshake> handshakes)
	{
		return map.get(component.getClass()).getComponentLayout(component, handshakes);
	}

	private static Map<Class<? extends Component>, Layouter<?>> getMap() {
		HashMap<Class<? extends Component>, Layouter<?>> map = new HashMap<Class<? extends Component>, Layouter<?>>();

		map.put(Adapt.class, new AdaptLayouter());
		map.put(While.class, new WhileLayouter());

		return map;
	}
}
