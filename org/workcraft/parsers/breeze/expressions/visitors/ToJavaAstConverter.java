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

import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.NameExpr;

import org.workcraft.parsers.breeze.expressions.AddExpression;
import org.workcraft.parsers.breeze.expressions.CaseExpression;
import org.workcraft.parsers.breeze.expressions.Constant;
import org.workcraft.parsers.breeze.expressions.ConstantArrayType;
import org.workcraft.parsers.breeze.expressions.ParameterReference;
import org.workcraft.parsers.breeze.expressions.StringConcatenateExpression;
import org.workcraft.parsers.breeze.expressions.ToStringExpression;
import org.workcraft.parsers.breeze.expressions.VariableArrayType;

public class ToJavaAstConverter implements Visitor<Expression> {

	private final NameExpr scope;

	public ToJavaAstConverter(NameExpr scope) {
		this.scope = scope;
	}

	@Override
	public Expression visit(AddExpression e) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public <T1, T2> Expression visit(CaseExpression<T1, T2> e) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public <T> Expression visit(Constant<T> e) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public <T> Expression visit(ParameterReference<T> e) {
		return new FieldAccessExpr(scope, e.getParameterName());
	}

	@Override
	public Expression visit(StringConcatenateExpression e) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public <T> Expression visit(ToStringExpression<T> e) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public Expression visit(VariableArrayType e) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public Expression visit(ConstantArrayType constantArrayType) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

}
