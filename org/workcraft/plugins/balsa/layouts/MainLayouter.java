/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.balsa.layouts;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.Adapt;
import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.components.SequenceOptimised;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class MainLayouter {

	static Map<Class<? extends Component>, Layouter<?>> map = getMap();

	public static HandshakeComponentLayout getLayout(Component component, Map<String, Handshake> handshakes)
	{
		if(handshakes == null)
			throw new NullPointerException("handshakes argument is null");
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
		map.put(Concur.class, new ConcurLayouter());
		map.put(SequenceOptimised.class, new SequenceOptimisedLayouter());

		return map;
	}
}
