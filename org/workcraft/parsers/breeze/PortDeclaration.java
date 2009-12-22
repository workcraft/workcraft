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

import org.workcraft.parsers.breeze.expressions.Expression;

import japa.parser.ast.body.VariableDeclarator;

public class PortDeclaration {

	public PortDeclaration(PortType type, String name, boolean isActive,
			boolean isInput, boolean isArrayed, Expression<Integer> count,
			Expression<Integer[]> width) {
		super();
		this.type = type;
		this.name = name;
		this.isActive = isActive;
		this.isInput = isInput;
		this.isArrayed = isArrayed;
		this.count = count;
		this.width = width;
	}

	public final PortType type;
	public final String name;
	public final boolean isActive;
	public final boolean isInput;

	public final boolean isArrayed;

	public final Expression<Integer> count;
	public final Expression<Integer[]> width;

	public String toString() {
		return String.format("(%s %s %s %s %s %s)", isArrayed?"ARRAYED-"+type:type, name, isActive?"active":"passive", isInput?"input":"output", count, width);
	}

	public String getName() {
		return name;
	}
}
