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

package org.workcraft.plugins.balsa.stg;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;

public class MainStgBuilder {
	public static void buildStg(Component component, Map<String, Handshake> handshakes, HandshakeStgBuilder builder)
	{
		Map<String, StgHandshake> handshakeStg = buildHandshakes(handshakes, builder);
		ComponentStgBuilder<?> componentBuilder = getComponentStgBuilder(component);
		componentBuilder.buildComponentStg(component, handshakeStg, builder.getStgBuilder());
	}

	private static Map<Class<? extends Component>, ComponentStgBuilder<?>> map = fillMap();

	@SuppressWarnings("unchecked")
	private static ComponentStgBuilder<?> getComponentStgBuilder(Component component) {
		Class<? extends Component> type = component.getClass();
		ComponentStgBuilder<?> result = map.get(type);
		while(result == null)
		{
			type = (Class<? extends Component>) type.getSuperclass();
			if(type == null)
				return null;
			result = map.get(type);
		}
		return result;
	}

	private static Map<Class<? extends Component>, ComponentStgBuilder<?>> fillMap() {
		Map<Class<? extends Component>, ComponentStgBuilder<?>> result = new HashMap<Class<? extends Component>, ComponentStgBuilder<?>>();

		result.put(org.workcraft.plugins.balsa.components.Concur.class, new ConcurStgBuilder());
		result.put(org.workcraft.plugins.balsa.components.SequenceOptimised.class, new SequenceOptimisedStgBuilder());
		result.put(org.workcraft.plugins.balsa.components.While.class, new WhileStgBuilder_NoDataPath());
		result.put(org.workcraft.plugins.balsa.components.BinaryFunc.class, new BinaryFuncStgBuilder_NoDataPath());
		result.put(org.workcraft.plugins.balsa.components.CallMux.class, new CallMux_NoDataPath());
		result.put(org.workcraft.plugins.balsa.components.Case.class, new Case_NoDataPath_OneBit());
		result.put(org.workcraft.plugins.balsa.components.Variable.class, new Variable_NoDataPath());
		result.put(org.workcraft.plugins.balsa.components.Fetch.class, new Fetch_NoDataPath());

		return result;
	}

	private static Map<String, StgHandshake> buildHandshakes(Map<String, Handshake> handshakes, HandshakeStgBuilder builder) {
		HashMap<String, StgHandshake> result = new HashMap<String, StgHandshake>();
		for(String key : handshakes.keySet())
			result.put(key, handshakes.get(key).buildStg(builder));
		return result;
	}
}
