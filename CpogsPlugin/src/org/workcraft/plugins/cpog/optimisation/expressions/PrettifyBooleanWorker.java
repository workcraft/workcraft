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

public class PrettifyBooleanWorker implements BooleanWorker {
	private final ReducedBooleanWorker worker;
	private static Zero ZERO = Zero.instance();
	private static One ONE = One.instance();

	public PrettifyBooleanWorker(ReducedBooleanWorker worker) {
		this.worker = worker;
	}

	public BooleanFormula not(BooleanFormula x) {
		if(x == ZERO)
			return ONE;
		if(x == ONE)
			return ZERO;
		return worker.not(x);
	}

	public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
		if(x==y)
			return ONE;
		if(x == ZERO)
			return not(y);
		if(x == ONE)
			return y;
		if(y == ZERO)
			return not(x);
		if(y == ONE)
			return x;
		return worker.iff(x, y);
	}

	public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
		return or(not(x), y);
	}

	public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
		if(x==y)
			return x;
		if(x == ZERO || y == ZERO)
			return ZERO;
		if(x == ONE)
			return y;
		if(y == ONE)
			return x;
		return worker.and(x,y);
	}

	public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
		return not(and(not(x), not(y)));
	}

	@Override
	public BooleanFormula one() {
		return ONE;
	}

	@Override
	public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
		return not(iff(x,y));
	}

	@Override
	public BooleanFormula zero() {
		return ZERO;
	}
}
