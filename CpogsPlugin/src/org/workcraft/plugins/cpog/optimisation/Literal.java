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
package org.workcraft.plugins.cpog.optimisation;

import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

public class Literal implements BooleanFormula
{
	private BooleanVariable variable;
	private boolean negation;

	public Literal()
	{
	}

	public static Literal Zero = new Literal(new FreeVariable("0"));
	public static Literal One = new Literal(new FreeVariable("1"));

	public Literal(BooleanVariable variable) {
		this.variable = variable;
	}

	public Literal(BooleanVariable variable, boolean negation) {
		this.variable = variable;
		this.negation = negation;
	}

	public Literal(String varName) {
		this(new FreeVariable(varName));
	}

	public void setVariable(BooleanVariable variable) {
		this.variable = variable;
	}

	public BooleanVariable getVariable() {
		return variable;
	}

	public void setNegation(boolean negation) {
		this.negation = negation;
	}

	public boolean getNegation() {
		return negation;
	}

	@Override
	public <T> T accept(BooleanVisitor<T> visitor) {
		return (negation?BooleanOperations.not(variable):variable).accept(visitor);
	}
}
