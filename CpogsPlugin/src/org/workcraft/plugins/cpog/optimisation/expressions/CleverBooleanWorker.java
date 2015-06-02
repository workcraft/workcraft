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
package org.workcraft.plugins.cpog.optimisation.expressions;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;

public class CleverBooleanWorker implements BooleanWorker
{
	private static final BooleanFormula ZERO = Zero.instance();
	private static final BooleanFormula ONE = One.instance();

	@Override
	public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
		if(FormulaToString.toString(x).equals(FormulaToString.toString(y)))
			return x;
		if(x == ZERO || y == ZERO)
			return ZERO;
		if(x == ONE)
			return y;
		if(y == ONE)
			return x;
		return new And(x,y);
	}

	@Override
	public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
		if(FormulaToString.toString(x).equals(FormulaToString.toString(y)))
			return ONE;
		if(x == ONE)
			return y;
		if(x == ZERO)
			return not(y);
		if(y == ONE)
			return x;
		if(y == ZERO)
			return not(x);
		return new Iff(x,y);
	}

	@Override
	public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
		if(FormulaToString.toString(x).equals(FormulaToString.toString(y)))
			return ONE;
		if(x == ZERO || y == ONE)
			return ONE;
		if(x == ONE)
			return y;
		if(y == ZERO)
			return not(x);
		return new Imply(x,y);
	}

	@Override
	public BooleanFormula not(BooleanFormula x) {
		if(x == ONE)
			return ZERO;
		if(x == ZERO)
			return ONE;
		return new Not(x);
	}

	@Override
	public BooleanFormula one() {
		return One.instance();
	}

	@Override
	public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
		if(FormulaToString.toString(x).equals(FormulaToString.toString(y)))
			return x;
		if (checkStrings(FormulaToString.toString(x), FormulaToString.toString(y)))
			return x;
 		if (checkStrings(FormulaToString.toString(x), invertString(FormulaToString.toString(y))))
			return ONE;
		if(x == ONE || y == ONE)
			return ONE;
		if(x == ZERO)
			return y;
		if(y == ZERO)
			return x;
		return new Or(x,y);
	}

	@Override
	public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
		if(FormulaToString.toString(x).equals(FormulaToString.toString(y)))
			return ZERO;
		if(x == ONE)
			return not(y);
		if(x == ZERO)
			return y;
		if(y == ONE)
			return not(x);
		if(y == ZERO)
			return x;
		return new Xor(x,y);
	}

	@Override
	public BooleanFormula zero() {
		return Zero.instance();
	}

	public boolean checkStrings(String x, String y) {
		if(x.contains(y)) {
			if (x.startsWith(y + " +")) {
				return true;
			} else if (x.endsWith("+ " + y)) {
				return true;
			} else if (x.contains(("+ " + y + " +"))) {
				return true;
			}
		}
		return false;
	}

	public String invertString(String x) {
		String result = "";

		//find first operator
		int op = x.indexOf("*");
		if ((x.indexOf(" + ") < op) && (x.indexOf(" + ") > 0)) {
			op = x.indexOf(" + ");
			if (x.substring(0, op).contains("'")) {
				result = x.substring(0, op).replace("'", "");
			} else {
				result = x.substring(0, op) + "'";
			}
			result = result + x.substring(op, op + 2) + invertString(x.substring(op + 2));
			return result;
		}
		if(op == -1) {
			if (x.contains("'")) {
				return x.replace("'", "");
			} else {
				return x + "'";
			}
		}

		if (x.substring(0, op).contains("'")) {
			result = x.substring(0, op).replace("'", "");
		} else {
			result = x.substring(0, op) + "'";
		}
		result = result + x.charAt(op) + invertString(x.substring(op + 1));
		return result;


	}
}
