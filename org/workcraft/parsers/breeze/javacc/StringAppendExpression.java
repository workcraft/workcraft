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

import org.workcraft.parsers.breeze.Expression;
import org.workcraft.parsers.breeze.ParameterScope;

public class StringAppendExpression implements Expression<String> {

	private final Expression<String> str1;
	private final Expression<String> str2;

	public StringAppendExpression(Expression<String> str1,
			Expression<String> str2) {
				this.str1 = str1;
				this.str2 = str2;
	}

	@Override
	public String evaluate(ParameterScope parameters) {
		return str1.evaluate(parameters) + str2.evaluate(parameters);
	}

	@Override public String toString() {
		return str1.toString() + " + " + str2.toString();
	}
}
