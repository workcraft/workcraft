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

package org.workcraft.parsers.breeze.expressions;

import java.util.Arrays;

import org.workcraft.parsers.breeze.ParameterScope;
import org.workcraft.parsers.breeze.expressions.visitors.Visitor;

public class ConstantArrayType implements Expression<Integer[]> {
	private final Expression<Integer> width;
	private final Expression<Integer> count;

	public ConstantArrayType(Expression<Integer> width, Expression<Integer> count) {
		this.width = width;
		this.count = count;
	}

	@Override
	public Integer[] evaluate(ParameterScope parameters) {
		Integer[] result = new Integer[getCount().evaluate(parameters)];
		Arrays.fill(result, getWidth().evaluate(parameters));
		return result;
	}

	public String toString() {
		return "(constant-array-type width " + getWidth() + " count " + getCount() +")";
	}


	@Override
	public <R> R accept(Visitor<R> visitor) {
		return visitor.visit(this);
	}

	public Expression<Integer> getWidth() {
		return width;
	}

	public Expression<Integer> getCount() {
		return count;
	}
}
