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

package org.workcraft.plugins.balsa.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum BinaryOperator
{
	ADD ("Add"),
	SUBTRACT ("Subtract"),
	REVERSE_SUBTRACT ("ReverseSubtract"),
	EQUALS ("Equals"),
	NOT_EQUALS ("NotEquals"),
	LESS_THAN ("LessThan"),
	GREATER_THAN ("GreaterThan"),
	LESS_OR_EQUALS ("LessOrEquals"),
	GREATER_OR_EQUALS ("GreaterOrEquals"),
	AND ("And"),
	OR ("Or");

	private static Map<String, BinaryOperator> textToValue = fillMap();
	public static Map<String, BinaryOperator> textToValue() { return Collections.unmodifiableMap(textToValue); }
	private static Map<String, BinaryOperator> fillMap() {
		Map<String, BinaryOperator> result = new HashMap<String, BinaryOperator>();

		for(BinaryOperator op : BinaryOperator.values())
			result.put(op.text, op);

		return result;
	}

	private final String text;
	private BinaryOperator(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

	public static BinaryOperator parse(String text)
	{
		BinaryOperator result = textToValue.get(text);
		if(result == null)
			throw new IndexOutOfBoundsException("Unknown binary operator: " + text);
		return result;
	}
};
