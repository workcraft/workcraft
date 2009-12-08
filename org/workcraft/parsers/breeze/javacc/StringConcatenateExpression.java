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
package org.workcraft.parsers.breeze.javacc;

import java.util.List;

import org.workcraft.parsers.breeze.Expression;
import org.workcraft.parsers.breeze.ParameterScope;

public class StringConcatenateExpression implements Expression<String> {

	private final List<Expression<String>> strs;

	public StringConcatenateExpression(List<Expression<String>> strs) {
				this.strs = strs;
	}

	@Override
	public String evaluate(ParameterScope parameters) {
		StringBuilder sb = new StringBuilder();
		for(Expression<String> str : strs)
			sb.append(str.evaluate(parameters));
		return sb.toString();
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Expression<String> str : strs)
		{
			if(sb.length() == 0)
				sb.append(" + ");
			sb.append(str.toString());
		}
		return sb.toString();
	}
}
