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

import java.util.HashMap;
import java.util.Map;

import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.parsers.breeze.expressions.Expression;

public interface ParameterType {
	Class<?> getJavaType();
	Object parse(String text);
}

 interface DataType extends ParameterType {
	Expression<Integer> getWidth();
}

class StringParameter implements ParameterType
{
	@Override public Class<?> getJavaType() {
		return String.class;
	}
	@Override public Object parse(String text) {
		return text;
	}
}

class CardinalParameter implements ParameterType
{
	@Override public Class<?> getJavaType() {
		return int.class;
	}
	@Override public Object parse(String text) {
		return Integer.parseInt(text);
	}
}

class BooleanParameter implements ParameterType
{
	@Override public Class<?> getJavaType() {
		return boolean.class;
	}
	@Override public Object parse(String text) {
		if(text.toLowerCase().equals("true"))
			return true;
		if(text.toLowerCase().equals("false"))
			return false;
		throw new NotSupportedException();
	}
}

class EnumParameter<T> implements ParameterType
{
	private final Class<T> type;
	private final Map<String, ? extends T> values;
	public EnumParameter(Class<T> type, Map<String, ? extends T> values)
	{
		this.type = type;
		HashMap<String, T> lowercasedValues = new HashMap<String, T>();
		for(String key : values.keySet())
			lowercasedValues.put(key.toLowerCase(), values.get(key));
		this.values = lowercasedValues;
	}
	@Override
	public Class<?> getJavaType() {
		return type;
	}
	@Override
	public Object parse(String text) {
		return values.get(text.toLowerCase());
	}
}
