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

import java.util.List;

public class PrimitivePart implements BreezeDefinition {
	String name;
	List<ParameterDeclaration> parameters;
	String symbol;
	List<PortDeclaration> ports;


	public PrimitivePart(String name, List<ParameterDeclaration> parameters,
			List<PortDeclaration> ports, String symbol) {
		super();
		this.name = name;
		this.parameters = parameters;
		this.symbol = symbol;
		this.ports = ports;
	}

	public String toString() {
		return "("+name+" " + parameters + " " + symbol + " " + ports + ")";
	}

	public List<PortDeclaration> ports() {
		return ports;
	}

	@Override public <Port> BreezeInstance<Port> instantiate(BreezeFactory<Port> factory, ParameterScope parameters) {
		return factory.create(this, parameters);
	}
}
