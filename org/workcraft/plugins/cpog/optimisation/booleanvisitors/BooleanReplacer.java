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
package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.FreeVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.*;

public class BooleanReplacer implements BooleanVisitor<BooleanFormula>
{
	private final HashMap<BooleanFormula, BooleanFormula> map;

	public BooleanReplacer(List<? extends FreeVariable> from, List<? extends BooleanFormula> to)
	{
		HashMap<BooleanFormula, BooleanFormula> m = new HashMap<BooleanFormula, BooleanFormula>();
		if(from.size() != to.size())
			throw new RuntimeException("lengths must be equal");
		for(int i=0;i<from.size();i++)
			m.put(from.get(i), to.get(i));
		map = m;
	}

	public BooleanReplacer(Map<? extends FreeVariable, ? extends BooleanFormula> map)
	{
		this.map = new HashMap<BooleanFormula, BooleanFormula> (map);
	}

	protected BooleanFormula visitBinaryFunc(BinaryBooleanFormula node, BinaryOperation op) {
		BooleanFormula result = map.get(node);
		if(result == null)
		{
			BooleanFormula x = node.getX().accept(this);
			BooleanFormula y = node.getY().accept(this);
			if(node.getX() == x && node.getY() == y)
				result = node;
			else
				result = op.apply(x, y);
			map.put(node, result);
		}
		return result;
	}

	interface BinaryOperation
	{
		public BooleanFormula apply(BooleanFormula x, BooleanFormula y);
	}

	@Override
	public BooleanFormula visit(And node) {
		return visitBinaryFunc(node, new BinaryOperation()
		{
			@Override
			public BooleanFormula apply(BooleanFormula x, BooleanFormula y) {
				return and(x,y);
			}
		});
	}

	@Override
	public BooleanFormula visit(Or node) {
		return visitBinaryFunc(node, new BinaryOperation()
		{
			@Override
			public BooleanFormula apply(BooleanFormula x, BooleanFormula y) {
				return or(x,y);
			}
		});
	}

	@Override
	public BooleanFormula visit(FreeVariable node) {
		BooleanFormula replacement = map.get(node);
		return replacement!=null? replacement : node;
	}

	public static BooleanFormula replace(BooleanFormula formula, List<? extends FreeVariable> params, List<? extends BooleanFormula> values) {
		return formula.accept(new BooleanReplacer(params, values));
	}

	@Override
	public BooleanFormula visit(Iff node) {
		return visitBinaryFunc(node, new BinaryOperation()
		{
			@Override
			public BooleanFormula apply(BooleanFormula x, BooleanFormula y) {
				return iff(x,y);
			}
		});
	}

	@Override
	public BooleanFormula visit(Zero node) {
		return node;
	}

	@Override
	public BooleanFormula visit(One node) {
		return node;
	}

	@Override
	public BooleanFormula visit(Not node) {
		BooleanFormula result = map.get(node);
		if(result == null)
		{
			BooleanFormula x = node.getX().accept(this);
			if(node.getX() == x)
				result = node;
			else
				result = not(x);

			map.put(node, result);
		}
		return result;
	}

	@Override
	public BooleanFormula visit(Imply node) {
		return visitBinaryFunc(node, new BinaryOperation()
		{
			@Override
			public BooleanFormula apply(BooleanFormula x, BooleanFormula y) {
				return imply(x,y);
			}
		});
	}
}
