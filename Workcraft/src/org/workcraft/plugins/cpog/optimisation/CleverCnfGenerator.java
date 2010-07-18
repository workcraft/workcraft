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

import static org.workcraft.plugins.cpog.optimisation.CnfOperations.literal;
import static org.workcraft.plugins.cpog.optimisation.CnfOperations.not;
import static org.workcraft.plugins.cpog.optimisation.CnfOperations.or;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.RecursiveBooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class CleverCnfGenerator implements RawCnfGenerator<BooleanFormula>, BooleanVisitor<CnfLiteral>
{
	Cnf result = new Cnf();

	private static class Void { private Void(){} }

	static class ConstantExpectingCnfGenerator implements BooleanVisitor<Void>
	{
		private static boolean cleverOptimiseAnd = true;

		private BooleanVisitor<CnfLiteral> dumbGenerator;
		private Cnf result;

		public ConstantExpectingCnfGenerator(Cnf result, BooleanVisitor<CnfLiteral> dumbGenerator)
		{
			this.result = result;
			this.dumbGenerator = dumbGenerator;
		}

		boolean currentResult = true;

		@Override
		public Void visit(And and)
		{
			if(currentResult)
			{
				and.getX().accept(this);
				and.getY().accept(this);
			}
			else
			{
				if(!cleverOptimiseAnd)
				{
					CnfLiteral x = and.getX().accept(dumbGenerator);
					CnfLiteral y = and.getY().accept(dumbGenerator);
					result.add(or(not(x), not(y)));
				}
				else
				{
					CnfLiteral[][] side1 = getBiClause(and.getX());
					CnfLiteral[][] side2 = getBiClause(and.getY());
					for(int i=0;i<side1.length;i++)
						for(int j=0;j<side2.length;j++)
						{
							List<CnfLiteral> list = new ArrayList<CnfLiteral>();
							for(int k=0;k<side1[i].length;k++)
								list.add(not(side1[i][k]));
							for(int k=0;k<side2[j].length;k++)
								list.add(not(side2[j][k]));
							result.add(new CnfClause(list));
						}
				}

			}
			return null;
		}

		static class BiClauseGenerator implements BooleanVisitor<CnfLiteral[][]>
		{
			BooleanVisitor<CnfLiteral> dumbGenerator;
			public BiClauseGenerator(BooleanVisitor<CnfLiteral> dumbGenerator)
			{
				this.dumbGenerator = dumbGenerator;
			}
			@Override
			public CnfLiteral[][] visit(And node) {
				CnfLiteral[][] result = new CnfLiteral[1][];
				result[0] = new CnfLiteral[2];
				result[0][0] = node.getX().accept(dumbGenerator);
				result[0][1] = node.getY().accept(dumbGenerator);
				return result;
			}
			@Override
			public CnfLiteral[][] visit(Iff node) {
				throw new RuntimeException();
			}
			@Override
			public CnfLiteral[][] visit(Zero node) {
				throw new RuntimeException();
			}
			@Override
			public CnfLiteral[][] visit(One node) {
				throw new RuntimeException();
			}
			@Override
			public CnfLiteral[][] visit(Not node) {
				CnfLiteral[][] preres = node.getX().accept(this);
				if(preres.length!=1)
					throw new RuntimeException("something wrong...");
				CnfLiteral[][]res = new CnfLiteral[preres[0].length][];
				for(int i=0;i<res.length;i++)
				{
					res[i] = new CnfLiteral[1];
					res[i][0] = CnfOperations.not(preres[0][i]);
				}
				return res;
			}
			@Override
			public CnfLiteral[][] visit(Imply node) {
				throw new RuntimeException();
			}
			@Override
			public CnfLiteral[][] visit(BooleanVariable variable) {
				CnfLiteral[][]result = new CnfLiteral[1][];
				result[0] = new CnfLiteral[1];
				result[0][0] = CnfOperations.literal(variable);
				return result;
			}
			@Override
			public CnfLiteral[][] visit(Or node) {
				throw new RuntimeException();
			}
			@Override
			public CnfLiteral[][] visit(Xor node) {
				throw new RuntimeException();
			}
		}

		CnfLiteral[][] getBiClause(BooleanFormula formula)
		{
			return formula.accept(new BiClauseGenerator(dumbGenerator));
		}

		@Override
		public Void visit(Iff iff)
		{
			CnfLiteral x = iff.getX().accept(dumbGenerator);
			CnfLiteral y = iff.getY().accept(dumbGenerator);
			if(currentResult)
			{
				result.add(or(x, not(y)));
				result.add(or(not(x), y));
			}
			else
			{
				result.add(or(x, y));
				result.add(or(not(x), not(y)));
			}
			return null;
		}

		@Override
		public Void visit(Not not)
		{
			boolean store = currentResult;
			currentResult = !currentResult;
			not.getX().accept(this);
			currentResult = store;
			return null;
		}

		@Override
		public Void visit(BooleanVariable node) {
			if(currentResult)
				result.add(or(literal(node)));
			else
				result.add(or(not(node)));
			return null;
		}
		@Override
		public Void visit(Zero node) {
			throw new RuntimeException("not implemented");
		}
		@Override
		public Void visit(One node) {
			throw new RuntimeException("not implemented");
		}
		@Override
		public Void visit(Imply node) {
			throw new RuntimeException("not implemented");
		}
		@Override
		public Void visit(Or node) {
			throw new RuntimeException("not implemented");
		}

		@Override
		public Void visit(Xor node) {
			throw new RuntimeException("not implemented");
		}
	}

	public CnfTask getCnf(BooleanFormula formula)
	{

		Cnf cnf = generateCnf(formula);
		return new SimpleCnfTaskProvider().getCnf(cnf);
	}

	class FormulaCounter extends RecursiveBooleanVisitor<Object>
	{
		int count = 0;

		Map<BooleanFormula, Integer> met = new HashMap<BooleanFormula, Integer>();

		/*@Override
		protected Object visitDefault(BooleanFormula node) {
		}*/

		@Override
		protected Object visitBinary(BinaryBooleanFormula node) {
			count++;
			Integer m = met.get(node);
			if(m == null)
				met.put(node,1);
			else
				met.put(node, m+1);
			return super.visitBinary(node);
		}

		public void printReport()
		{
			for(Entry<BooleanFormula, Integer> entry : met.entrySet())
			{
				if(entry.getValue() > 100)
				{
					System.out.println(">1000: " + entry.getValue() + ": " + FormulaToString.toString(entry.getKey()));
				}
			}
		}

		public int getCount()
		{
			return count;
		}

		public int getUniques()
		{
			return met.size();
		}
	}


	public Cnf generateCnf(BooleanFormula formula) {
		//FormulaCounter counter = new FormulaCounter();
		//formula.accept(counter);
		//System.out.println("total visits: " + counter.getCount());
		//System.out.println("unique visits: " + counter.getUniques());
		//counter.printReport();
		//System.out.println("formula: " + FormulaToString.toString(formula));

		formula.accept(new ConstantExpectingCnfGenerator(result, this));
		//CnfLiteral res = formula.accept(this);result.add(or(res));

		return result;
	}

	Map<BooleanFormula, CnfLiteral> cache = new HashMap<BooleanFormula, CnfLiteral>();
	//int varCount = 0;

	CnfLiteral newVar(BooleanFormula node)
	{
		CnfLiteral res = new CnfLiteral("");//"tmp_"+varCount++
		cache.put(node, res);
		return res;
	}

	interface BinaryGateImplementer
	{
		void implement(CnfLiteral res, CnfLiteral x, CnfLiteral y);
	}

	public CnfLiteral visit(BinaryBooleanFormula node, BinaryGateImplementer impl) {
		CnfLiteral res = cache.get(node);
		if(res == null)
		{
			res = newVar(node);
			CnfLiteral x = node.getX().accept(this);
			CnfLiteral y = node.getY().accept(this);
			impl.implement(res, x, y);
		}
		return res;
	}

	@Override
	public CnfLiteral visit(And node) {
		return visit(node,
			new BinaryGateImplementer()
			{
				@Override public void implement(CnfLiteral res, CnfLiteral x, CnfLiteral y) {
					result.add(or(res, not(x), not(y)));
					result.add(or(not(res), x));
					result.add(or(not(res), y));
				}
			}
		);
	}

	@Override
	public CnfLiteral visit(Iff node) {
		return visit(node,
				new BinaryGateImplementer()
				{
					@Override public void implement(CnfLiteral res, CnfLiteral x, CnfLiteral y) {
						result.add(or(not(res), not(x), y));
						result.add(or(not(res), x, not(y)));
						result.add(or(res, not(x), not(y)));
						result.add(or(res, x, y));
					}
				}
			);
	}

	@Override
	public CnfLiteral visit(Zero node) {
		return CnfLiteral.Zero;
	}

	@Override
	public CnfLiteral visit(One node) {
		return CnfLiteral.One;
	}

	@Override
	public CnfLiteral visit(Not node) {
		return not(node.getX().accept(this));
	}

	@Override
	public CnfLiteral visit(Imply node) {
		return visit(node,
				new BinaryGateImplementer()
				{
					@Override public void implement(CnfLiteral res, CnfLiteral x, CnfLiteral y) {
						result.add(or(not(res), not(x), y));
						result.add(or(res, not(y)));
						result.add(or(res, x));
					}
				}
			);
	}

	@Override
	public CnfLiteral visit(BooleanVariable variable) {
		return literal(variable);
	}

	@Override
	public CnfLiteral visit(Or node) {
		return visit(node,
				new BinaryGateImplementer()
				{
					@Override public void implement(CnfLiteral res, CnfLiteral x, CnfLiteral y) {
						result.add(or(not(res), x, y));
						result.add(or(res, not(y)));
						result.add(or(res, not(x)));
					}
				}
			);
	}

	@Override
	public CnfLiteral visit(Xor node) {
		return visit(node,
				new BinaryGateImplementer()
				{
					@Override public void implement(CnfLiteral res, CnfLiteral x, CnfLiteral y) {
						result.add(or(res, not(x), y));
						result.add(or(res, x, not(y)));
						result.add(or(not(res), not(x), not(y)));
						result.add(or(not(res), x, y));
					}
				}
			);
	}
}
