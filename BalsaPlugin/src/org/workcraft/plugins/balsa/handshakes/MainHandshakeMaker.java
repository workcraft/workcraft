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

package org.workcraft.plugins.balsa.handshakes;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class MainHandshakeMaker {
	static Map<Class<? extends Component>, HandshakeMaker<?>> map = getMap();

	@SuppressWarnings("unchecked")
	public static Map<String, Handshake> getHandshakes(Component component)
	{
		Class<? extends Component> type = component.getClass();
		HandshakeMaker<?> maker;

		maker = map.get(type);
		while(maker == null)
		{
			type = (Class<? extends Component>)type.getSuperclass();
			if(type == null)
				return new HashMap<String, Handshake>();
			maker = map.get(type);
		}

		return maker.getComponentHandshakes(component);
	}

	private static Map<Class<? extends Component>, HandshakeMaker<?>> getMap() {
		HashMap<Class<? extends Component>, HandshakeMaker<?>> map = new HashMap<Class<? extends Component>, HandshakeMaker<?>>();

		map.put(org.workcraft.plugins.balsa.components.DynamicComponent.class, new DynamicHandshakes());

		return map;
	}
}
