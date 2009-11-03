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

import java.util.Map;

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakes.BinaryFuncHandshakes;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class BinaryFuncStgBuilder extends
		DataPathComponentStgBuilder<BinaryFunc> {

	BinaryFuncStgBuilder_Arithmetic arithmetic = new BinaryFuncStgBuilder_Arithmetic();
	BinaryFuncStgBuilder_Comparison comparison = new BinaryFuncStgBuilder_Comparison();

	DataPathComponentStgBuilder<BinaryFunc> get(BinaryFunc component)
	{
		if(BinaryFuncHandshakes.isComparison(component))
			return comparison;
		else
			return arithmetic;
	}

	public void buildStg(BinaryFunc component, Map<String, StgInterface> handshakes, StgInterface dataPath, StrictPetriBuilder builder) {
		get(component).buildStg(component, handshakes, dataPath, builder);
	}

	@Override
	public Handshake getDataPathHandshake(BinaryFunc component) {
		return get(component).getDataPathHandshake(component);
	}
}
