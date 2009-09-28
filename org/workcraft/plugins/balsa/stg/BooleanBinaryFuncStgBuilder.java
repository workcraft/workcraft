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

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.BinaryOperator;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stg.op.AndStgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class BooleanBinaryFuncStgBuilder extends
		ComponentStgBuilder<BinaryFunc> {

	public BooleanBinaryFuncStgBuilder()
	{
		opBuilders.put(BinaryOperator.AND, new AndStgBuilder());
	}

	HashMap<BinaryOperator, ComponentStgBuilder<BinaryFunc>> opBuilders = new HashMap<BinaryOperator, ComponentStgBuilder<BinaryFunc>>();

	@Override
	public void buildStg(BinaryFunc component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder) {
		ComponentStgBuilder<BinaryFunc> opBuilder = opBuilders.get(component.getOp());
		opBuilder.buildStg(component, handshakes, builder);
	}
}
