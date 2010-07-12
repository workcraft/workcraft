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

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.and;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.not;
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.or;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class BinaryNumberProvider implements
		NumberProvider<BinaryIntBooleanFormula> {

	@Test
	public void testBigConstraint()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		p.generate("x", 25);
		Assert.assertEquals("(!xb4|(!xb3|(!xb2&(!xb1&!xb0))))", FormulaToString.toString(p.getConstraints()));
	}


	@Test
	public void testValuesCount()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula num = p.generate("", 9);
		Assert.assertEquals(9, num.getValuesCount());
	}
	@Test
	public void testBigSelect()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula num = p.generate("", 9);
		BooleanFormula [] f = new BooleanFormula [9];
		f[0] = new FreeVariable("a");
		f[1] = new FreeVariable("b");
		f[2] = new FreeVariable("c");
		f[3] = new FreeVariable("d");
		f[4] = new FreeVariable("e");
		f[5] = new FreeVariable("f");
		f[6] = new FreeVariable("g");
		f[7] = new FreeVariable("h");
		f[8] = new FreeVariable("i");
		BooleanFormula result = p.select(f, num);
//		System.out.println(result.accept(new FormulaToString()));
		Assert.assertEquals("((b3&((b2&0)|(!b2&((b1&0)|(!b1&((b0&0)|(!b0&i)))))))|(!b3&((b2&((b1&((b0&h)|(!b0&g)))|(!b1&((b0&f)|(!b0&e)))))|(!b2&((b1&((b0&d)|(!b0&c)))|(!b1&((b0&b)|(!b0&a))))))))", result.accept(new FormulaToString()));
	}

	@Test
	public void testEmptyConstraint()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		p.generate("x", 2);
		Assert.assertEquals("1", p.getConstraints().accept(new FormulaToString()));
	}

	@Test
	public void testZeroBitEmptyConstraint()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		p.generate("", 1);
		Assert.assertEquals("1", p.getConstraints().accept(new FormulaToString()));
	}

	@Test
	public void testSelectZeroBit()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula num = p.generate("", 1);
		BooleanFormula [] f = new BooleanFormula [1];
		f[0] = new FreeVariable("x");
		BooleanFormula result = p.select(f, num);
		Assert.assertEquals("x", result.accept(new FormulaToString()));
	}

	@Test
	public void testSelectOneBit()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula num = p.generate("", 2);
		BooleanFormula [] f = new BooleanFormula [2];
		f[0] = new FreeVariable("x");
		f[1] = new FreeVariable("y");
		BooleanFormula result = p.select(f, num);
		Assert.assertEquals("((b0&y)|(!b0&x))", result.accept(new FormulaToString()));
	}

	@Test
	public void testSelectThreeValues()
	{
		BinaryNumberProvider p = new BinaryNumberProvider();
		BinaryIntBooleanFormula num = p.generate("", 3);
		BooleanFormula [] f = new BooleanFormula [3];
		f[0] = new FreeVariable("x");
		f[1] = new FreeVariable("y");
		f[2] = new FreeVariable("z");
		BooleanFormula result = p.select(f, num);
		Assert.assertEquals("((b1&((b0&0)|(!b0&z)))|(!b1&((b0&y)|(!b0&x))))", result.accept(new FormulaToString()));
	}

	List<BooleanFormula> constraints = new ArrayList<BooleanFormula>();

	@Override
	public BinaryIntBooleanFormula generate(String varPrefix, int range) {
		if(range == 0)
			throw new RuntimeException("range=0");
		int varCount = 0;
		int tmp = range-1;
		while(tmp>0)
		{
			tmp/=2;
			varCount++;
		}

		List<BooleanVariable> vars = new ArrayList<BooleanVariable>();
		for(int i=0;i<varCount;i++)
			vars.add(new FreeVariable(varPrefix + "b"+i));

		if(1<<varCount != range)
			constraints.add(less(vars, varCount-1, range));

		return new BinaryIntBooleanFormula(vars, range);
	}

	private BooleanFormula less(List<BooleanVariable> a, int n, int b) {
		BooleanVariable an = a.get(n);
		boolean bn = ((b>>n)&1) > 0;
		BooleanFormula nan = not(an);

		if(n==0)
			if(bn)
				return nan;
			else
				return Zero.instance();

		BooleanFormula L = less(a, n-1, b);

		if(bn)
			return or(nan, L);
		else
			return and(nan, L);
	}

	@Override
	public BooleanFormula getConstraints() {
		return and(constraints);
	}

	@Override
	public BooleanFormula select(BooleanFormula[] vars, BinaryIntBooleanFormula number) {
		List<BooleanVariable> bits = number.getVars();
		if(number.getValuesCount() != vars.length)
			throw new RuntimeException("lengths do not match: vars=" + vars.length + ", number="+number.getValuesCount());
		return select(vars, bits, bits.size(), 0, number.getValuesCount());
	}

	private BooleanFormula select(BooleanFormula[] vars, List<BooleanVariable> bits, int length, int offset, int threshold) {
		if(offset >= threshold)
			return Zero.instance();
		if(length == 0)
			return vars[offset];
		BooleanVariable x = bits.get(length-1);
		BooleanFormula nx = not(x);
		return or(
				and(x, select(vars, bits, length-1, offset+(1<<(length-1)), threshold)),
				and(nx, select(vars, bits, length-1, offset, threshold))
		);
	}

	@Override
	public BooleanFormula less(BinaryIntBooleanFormula a,
			BinaryIntBooleanFormula b) {
		return less(a.getVars(), b.getVars(), a.getVars().size()-1);
	}

	private BooleanFormula less(List<BooleanVariable> a, List<BooleanVariable> b, int n) {
		BooleanVariable an = a.get(n);
		BooleanFormula bn = b.get(n);
		BooleanFormula nan = not(an);

		if(n==0)
			return and(nan, bn);

		BooleanFormula L = less(a, b, n-1);

		return or(and(nan, bn), and(or(nan, bn), L));
	}

}
