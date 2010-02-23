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

public class ParameterReference<T> implements Expression<T> {
	private final String parameterName;

	public ParameterReference(String parameterName)
	{
		if(parameterName == null)
			throw new NullPointerException();
		this.parameterName = parameterName;
	}

	@SuppressWarnings("unchecked")
	public T evaluate(ParameterScope parameters)
	{
		return (T)parameters.get(getParameterName());
	}

	public String toString() {
		return "<" + getParameterName() +">";
	}

	@Override
	public <R> R accept(Visitor<R> visitor) {
		return visitor.visit(this);
	}

	public String getParameterName() {
		return parameterName;
	}
}
