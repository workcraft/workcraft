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
package org.workcraft.parsers.breeze.expressions.visitors;

import org.workcraft.parsers.breeze.expressions.*;

public interface Visitor<R> {
	public R visit(AddExpression e);
	public <T1, T2> R visit(CaseExpression<T1, T2> e);
	public <T> R visit(Constant<T> e);
	public <T> R visit(ParameterReference<T> e);
	public R visit(StringConcatenateExpression e);
	public <T> R visit(ToStringExpression<T> e);
	public R visit(VariableArrayType e);
	public R visit(ConstantArrayType constantArrayType);
}
