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
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public abstract class DataPathComponentStgBuilder <T> extends ComponentStgBuilder<T> {
	public final void buildStg(T component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder)
	{
		Map<String, StgInterface> newMap = new HashMap<String, StgInterface>(handshakes);
		StgInterface dp = newMap.get("dp");
		newMap.remove("dp");
		buildStg(component, newMap, dp, builder);
	}

	public abstract void buildStg(T component, Map<String, StgInterface> handshakes, StgInterface dpHandshake, StrictPetriBuilder builder);

	@SuppressWarnings("unchecked")
	public final Handshake getDataPathHandshake(Component component)
	{
		return getDataPathHandshake((T)component);
	}
	public abstract Handshake getDataPathHandshake(T component);
}
