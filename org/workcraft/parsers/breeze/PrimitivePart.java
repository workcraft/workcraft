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

import org.workcraft.exceptions.NotSupportedException;

public class PrimitivePart implements BreezeDefinition {
	String name;
	List<ParameterDeclaration> parameters;
	Expression<String> symbol;
	List<PortDeclaration> ports;


	public PrimitivePart(String name, List<ParameterDeclaration> parameters,
			List<PortDeclaration> ports, Expression<String> symbol) {
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

	@Override public <Port> BreezeInstance<Port> instantiate(BreezeLibrary library, BreezeFactory<Port> factory, ParameterValueList parameters) {
		return factory.create(this, parseParameters(parameters));
	}

	private ParameterScope parseParameters(ParameterValueList parameterValues) {

		MapParameterScope result = new MapParameterScope();

		if(parameters.size() != parameterValues.size())
			throw new RuntimeException("Incorrect number of parameter values for component " + name + " (expected " + parameters.size() + ", got " + parameterValues.size() + ")");

		for(int i=0;i<parameters.size();i++)
		{
			ParameterDeclaration parameter = parameters.get(i);
			Object value = parse(parameter, parameterValues.get(i));
			result.put(parameter.name(), value);
		}

		return result;
	}

	private static Object parse(ParameterDeclaration parameter, String string) {
		ParameterType type = parameter.type();
		if(type == ParameterType.BOOLEAN)
			return parseBoolean(string);
		if(type == ParameterType.CARDINAL)
			return parseCardinal(string);
		if(type == ParameterType.STRING)
			return string;
		if(type == ParameterType.OTHER)
			return string;
		throw new NotSupportedException();
	}

	private static Object parseCardinal(String string) {
		return Integer.parseInt(string);
	}

	private static Object parseBoolean(String string) {
		if(string.toLowerCase().equals("true"))
			return true;
		if(string.toLowerCase().equals("false"))
			return false;
		throw new NotSupportedException();
	}

	public Expression<String> getSymbol() {
		return symbol;
	}
}
