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

package org.workcraft.parsers.breeze;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeHandshake;
import org.workcraft.plugins.balsa.components.DynamicComponent;

public class DefaultBreezeFactory implements BreezeFactory<BreezeHandshake> {

	private final BalsaCircuit circuit;

	public DefaultBreezeFactory(BalsaCircuit circuit) {
		this.circuit = circuit;
	}

	@Override
	public void connect(BreezeHandshake port1, BreezeHandshake port2) {
		try {
			circuit.connect(port1, port2);
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public BreezeInstance<BreezeHandshake> create(PrimitivePart declaration, ParameterScope parameters)
	{
		BreezeComponent comp = new BreezeComponent();
		comp.setUnderlyingComponent(new DynamicComponent(declaration, parameters));
		circuit.add(comp);
		final ArrayList<BreezeHandshake> result = new ArrayList<BreezeHandshake>();
		for(BreezeHandshake hs : comp.getHandshakeComponents().values())
			result.add(hs);
		return new BreezeInstance<BreezeHandshake>()
		{
			@Override public List<BreezeHandshake> ports() {
				return result;
			}
		};
	}

}
