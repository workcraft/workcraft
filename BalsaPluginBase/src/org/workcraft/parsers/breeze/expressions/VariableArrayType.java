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

import org.workcraft.parsers.breeze.ParameterScope;
import org.workcraft.parsers.breeze.expressions.visitors.Visitor;

public class VariableArrayType implements Expression<Integer[]> {
	private final Expression<Integer> width;
	private final Expression<Integer> readPortCount;
	private final Expression<String> specification;

	public VariableArrayType(Expression<Integer> width, Expression<Integer> readPortCount,
			Expression<String> specification)
	{
		this.width = width;
		this.readPortCount = readPortCount;
		this.specification = specification;
	}

	public Integer[] evaluate(ParameterScope parameters)
	{
		Integer[] result = new Integer[readPortCount.evaluate(parameters)];

		if (specification.evaluate(parameters).length() != 0 )
			throw new RuntimeException ("Specification is not supported");

		for (int i=0; i < result.length; i++)
			result[i] = getWidth().evaluate(parameters);

		return result;
	}

	public String toString() {
		return String.format("(width %s readPortCount %s specification %s)", getWidth(), readPortCount, specification);
	}

	@Override
	public <R> R accept(Visitor<R> visitor) {
		return visitor.visit(this);
	}

	public Expression<Integer> getWidth() {
		return width;
	}

	public Expression<Integer> getReadPortCount() {
		return readPortCount;
	}

	public Expression<String> getSpecification() {
		return specification;
	}
}
