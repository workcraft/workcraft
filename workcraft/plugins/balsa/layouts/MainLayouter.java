package org.workcraft.plugins.balsa.layouts;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.Adapt;
import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class MainLayouter {

	static Map<Class<? extends Component>, Layouter<?>> map = getMap();

	public static HandshakeComponentLayout getLayout(Component component, Map<String, Handshake> handshakes)
	{
		Layouter<?> layouter = map.get(component.getClass());
		if(layouter == null)
			return getDefaultLayout(handshakes);
		return layouter.getComponentLayout(component, handshakes);
	}

	private static HandshakeComponentLayout getDefaultLayout(
			final Map<String, Handshake> handshakes) {
		return new HandshakeComponentLayout()
		{
			public Handshake getBottom() {
				return null;
			}

			public Handshake[][] getLeft() {
				return new Handshake[][]{new Handshake[]{}};
			}

			public Handshake[][] getRight() {
				Handshake[] allHandshakes = new Handshake[handshakes.size()];
				handshakes.values().toArray(allHandshakes);
				return new Handshake[][]{allHandshakes};
			}

			public Handshake getTop() {
				return null;
			}
		};
	}

	private static Map<Class<? extends Component>, Layouter<?>> getMap() {
		HashMap<Class<? extends Component>, Layouter<?>> map = new HashMap<Class<? extends Component>, Layouter<?>>();

		map.put(Adapt.class, new AdaptLayouter());
		map.put(While.class, new WhileLayouter());
		map.put(BinaryFunc.class, new BinaryFuncLayouter());

		return map;
	}
}
