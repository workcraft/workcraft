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
import org.workcraft.parsers.breeze.dom.PortDeclaration;
import org.workcraft.parsers.breeze.expressions.Expression;
import org.workcraft.plugins.balsa.components.BinaryOperator;
import org.workcraft.plugins.balsa.components.UnaryOperator;

public class PrimitivePart implements BreezeDefinition {
	private final String name;
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
		return "("+getName()+" " + parameters + " " + symbol + " " + ports + ")";
	}

	public List<PortDeclaration> getPorts() {
		return ports;
	}

	@Override public <Port> BreezeInstance<Port> instantiate(BreezeLibrary library, BreezeFactory<Port> factory, ParameterValueList parameters) {
		return factory.create(this, parseParameters(parameters));
	}

	private ParameterScope parseParameters(ParameterValueList parameterValues)
	{
		MapParameterScope result = new MapParameterScope();

		if(parameters.size() != parameterValues.size())
			throw new RuntimeException("Incorrect number of parameter values for component " + getName() + " (expected " + parameters.size() + ", got " + parameterValues.size() + ")");

		for(int i=0;i<parameters.size();i++)
		{
			ParameterDeclaration parameter = parameters.get(i);
			Object value = parse(parameter, parameterValues.get(i));
			result.put(parameter.getName(), value);
		}

		return result;
	}

	private static Object parse(ParameterDeclaration parameter, String string) {
		ParameterType type = parameter.getType();
		if(type == ParameterType.BOOLEAN)
			return parseBoolean(string);
		else if(type == ParameterType.CARDINAL)
			return Integer.parseInt(string);
		else if(type == ParameterType.STRING)
			return string;
		else if(type == ParameterType.BINARY_OPERATOR)
			return BinaryOperator.parse(string);
		else if(type == ParameterType.UNARY_OPERATOR)
			return UnaryOperator.valueOf(string);
		else
			throw new NotSupportedException();
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

	public String getName() {
		return name;
	}

	public List<ParameterDeclaration> getParameters() {
		return parameters;
	}
}
