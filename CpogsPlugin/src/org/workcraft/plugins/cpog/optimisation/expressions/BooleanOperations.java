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

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;


public class BooleanOperations
{
	public static BooleanWorker worker = new BooleanWorkerPrettifier(new MemoryConservingBooleanWorker());
	//private static BooleanWorker worker = new DumbBooleanWorker();

	public static BooleanFormula ZERO = worker.zero();
	public static BooleanFormula ONE = worker.one();

	public static BooleanFormula not(BooleanFormula x) {
		return worker.not(x);
	}

	public static BooleanFormula createAnd(List<? extends BooleanFormula> conditions) {
		return createAnd(conditions, 0, conditions.size());
	}

	private static BooleanFormula createAnd(List<? extends BooleanFormula> conditions, int start, int end) {
		int size = end-start;
		if(size == 0)
			return ONE;
		else
			if(size == 1)
				return conditions.get(start);
			else
			{
				int split = (end+start)/2;
				return and(createAnd(conditions, start, split), createAnd(conditions, split, end));
			}
	}

	public static BooleanFormula createOr(List<? extends BooleanFormula> conditions) {
		return createOr(conditions, 0, conditions.size());
	}

	private static BooleanFormula createOr(List<? extends BooleanFormula> conditions, int start, int end) {
		int size = end-start;
		if(size == 0)
			return ONE;
		else
			if(size == 1)
				return conditions.get(start);
			else
			{
				int split = (end+start)/2;
				return or(createOr(conditions, start, split), createOr(conditions, split, end));
			}
	}

	public static BooleanFormula and(List<? extends BooleanFormula> conditions) {
		return createAnd(conditions);
	}

	public static BooleanFormula or(BooleanFormula... conditions) {
		return or(Arrays.asList(conditions));
	}

	public static BooleanFormula or(List<? extends BooleanFormula> conditions) {
		return createOr(conditions);
	}

	public static BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
		return worker.iff(x, y);
	}

	public static BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
		return worker.xor(x, y);
	}

	public static BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
		return worker.imply(x, y);
	}

	public static BooleanFormula and(BooleanFormula x, BooleanFormula y) {
		return worker.and(x, y);
	}

	public static BooleanFormula or(BooleanFormula x, BooleanFormula y) {
		return worker.or(x, y);
	}
}
