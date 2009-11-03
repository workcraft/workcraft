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
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivenessSelector;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeProtocol;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.handshakestgbuilder.TwoSideStg;
import org.workcraft.plugins.balsa.protocols.StgBuilderForHandshakesImpl;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class MainStgBuilder {

	public static void addDataPathHandshakes(Map<String, Handshake> fullHandshakes, Component component) {
		ComponentStgBuilder<?> componentBuilder = getComponentStgBuilder(component);

		if(componentBuilder instanceof DataPathComponentStgBuilder<?>)
		{
			DataPathComponentStgBuilder<?> dpComponentBuilder = (DataPathComponentStgBuilder<?>)componentBuilder;
			Handshake hs = dpComponentBuilder.getDataPathHandshake(component);
			fullHandshakes.put("dp", hs);
		}
	}

	public static void buildStg(Component component, Map<String, TwoSideStg> handshakeStg, StgBuilder builder)
	{
		Map<String, StgInterface> hsFromInside = new HashMap<String, StgInterface>();
		Map<String, StgInterface> hsFromOutside = new HashMap<String, StgInterface>();

		for(String key : handshakeStg.keySet())
		{
			TwoSideStg twoWayHs = handshakeStg.get(key);
			hsFromInside.put(key, twoWayHs.internal);
			hsFromOutside.put(key, twoWayHs.external);
		}

		StrictPetriBuilder insideBuilder = new StrictPetriBuilderImpl(builder);
		StrictPetriBuilder envBuilder = insideBuilder;

		ComponentStgBuilder<?> componentBuilder = getComponentStgBuilder(component);
		componentBuilder.buildComponentStg(component, hsFromInside, insideBuilder);
		componentBuilder.buildEnvironmentConstraint(component, hsFromOutside, envBuilder);
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
		result.put(org.workcraft.plugins.balsa.components.BinaryFunc.class, new BinaryFuncStgBuilder());
		result.put(org.workcraft.plugins.balsa.components.CallMux.class, new CallMux_NoDataPath());
		result.put(org.workcraft.plugins.balsa.components.Case.class, new CaseStgBuilder());
		result.put(org.workcraft.plugins.balsa.components.Variable.class, new Variable_NoDataPath());
		result.put(org.workcraft.plugins.balsa.components.Fetch.class, new FetchStgBuilder());

		return result;
	}

	public static Map<String, TwoSideStg> buildHandshakes(Map<String, Handshake> handshakes, HandshakeProtocol protocol, StgBuilder builder) {
		HandshakeStgBuilder activeBuilder = protocol.get(new StgBuilderForHandshakesImpl(builder, true));
		HandshakeStgBuilder passiveBuilder = protocol.get(new StgBuilderForHandshakesImpl(builder, false));

		HashMap<String, TwoSideStg> result = new HashMap<String, TwoSideStg>();
		for(String key : handshakes.keySet())
		{
			Handshake hs = handshakes.get(key);
			boolean isActive = hs.isActive();
			TwoSideStg res = ActivenessSelector.direct(hs.buildStg(isActive ? activeBuilder : passiveBuilder), isActive);
			result.put(key, res);
		}
		return result;
	}
}
