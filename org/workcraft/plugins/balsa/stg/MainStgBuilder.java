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
import org.workcraft.plugins.balsa.handshakeevents.TwoWayStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivenessSelector;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeProtocol;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.protocols.StgBuilderForHandshakesImpl;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class MainStgBuilder {

	public static void buildStg(Component component, Map<String, Handshake> handshakes, StgBuilder builder, HandshakeProtocol protocol)
	{
		Map<String, Handshake> handshakesCopy = new HashMap<String, Handshake>(handshakes);

		HandshakeStgBuilder activeHandshakeBuilder = protocol.get(new StgBuilderForHandshakesImpl(builder, true));
		HandshakeStgBuilder passiveHandshakeBuilder = protocol.get(new StgBuilderForHandshakesImpl(builder, false));

		ComponentStgBuilder<?> componentBuilder = getComponentStgBuilder(component);
		if(componentBuilder instanceof DataPathComponentStgBuilder<?>)
		{
			DataPathComponentStgBuilder<?> dpComponentBuilder = (DataPathComponentStgBuilder<?>)componentBuilder;
			Handshake hs = dpComponentBuilder.getDataPathHandshake(component);
			handshakesCopy.put("dp", hs);
		}

		Map<String, TwoWayStg> handshakeStg = buildHandshakes(handshakesCopy, activeHandshakeBuilder, passiveHandshakeBuilder);


		Map<String, StgInterface> hsFromInside = new HashMap<String, StgInterface>();
		Map<String, StgInterface> hsFromOutside = new HashMap<String, StgInterface>();

		for(String key : handshakeStg.keySet())
		{
			boolean isActive = handshakesCopy.get(key).isActive();
			TwoWayStg twoWayHs = handshakeStg.get(key);
			StgInterface active = ActivenessSelector.active(twoWayHs);
			StgInterface passive = ActivenessSelector.passive(twoWayHs);
			StgInterface inside, outside;
			inside = isActive ? active : passive;
			outside = isActive ? passive : active;
			hsFromInside.put(key, inside);
			hsFromOutside.put(key, outside);
		}

		StrictPetriBuilder insideBuilder = new StrictPetriBuilderImpl(builder);
		StrictPetriBuilder envBuilder = insideBuilder;

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
		result.put(org.workcraft.plugins.balsa.components.BinaryFunc.class, new BinaryFuncStgBuilder_NoDataPath());
		result.put(org.workcraft.plugins.balsa.components.CallMux.class, new CallMux_NoDataPath());
		result.put(org.workcraft.plugins.balsa.components.Case.class, new CaseStgBuilder());
		result.put(org.workcraft.plugins.balsa.components.Variable.class, new Variable_NoDataPath());
		result.put(org.workcraft.plugins.balsa.components.Fetch.class, new FetchStgBuilder());

		return result;
	}

	private static Map<String, TwoWayStg> buildHandshakes(Map<String, Handshake> handshakes, HandshakeStgBuilder activeBuilder, HandshakeStgBuilder passiveBuilder) {
		HashMap<String, TwoWayStg> result = new HashMap<String, TwoWayStg>();
		for(String key : handshakes.keySet())
		{
			Handshake hs = handshakes.get(key);
			TwoWayStg res = hs.buildStg(hs.isActive() ? activeBuilder : passiveBuilder);
			result.put(key, res);
		}
		return result;
	}
}
