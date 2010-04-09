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


public class FormulaToString implements BooleanVisitor<String>
{
	static class PrinterSuite
	{
		public PrinterSuite()
		{
			builder = new StringBuilder();
			Map<String, FreeVariable> varMap = new HashMap<String, FreeVariable>();
			IffPrinter = new Printer(this, 0, builder, varMap);
			ImplyPrinter = new Printer(this, 1, builder, varMap);
			OrPrinter = new Printer(this, 2, builder, varMap);
			AndPrinter = new Printer(this, 3, builder, varMap);
			NotPrinter = new Printer(this, 4, builder, varMap);
		}
		public Printer IffPrinter;
		public Printer ImplyPrinter;
		public Printer OrPrinter;
		public Printer AndPrinter;
		public Printer NotPrinter;
		private StringBuilder builder;
	}

	//private static FormulaToString BasicPrinter = new FormulaToString(5);
	private static class Printer implements BooleanVisitor<Object>
	{
		private final int level;
		private final PrinterSuite suite;
		private final StringBuilder builder;
		private final Map<String, FreeVariable> varMap;

		public Printer(PrinterSuite suite, int level, StringBuilder builder, Map<String, FreeVariable> varMap)
		{
			this.suite = suite;
			this.level = level;
			this.builder = builder;
			this.varMap = varMap;
		}

		@Override
		public Object visit(And node) {
			visitBinary(suite.AndPrinter.level, suite.AndPrinter, "&", node);
			return null;
		}

		@Override
		public String visit(Iff node) {
			visitBinary(suite.IffPrinter.level, suite.IffPrinter, "<->", node);
			return null;
		}

		private void visitBinary(int opLevel, Printer opPrinter, String opSymbol, BinaryBooleanFormula node) {
			boolean paren = level>opLevel;
			if(paren)
				builder.append("(");
			node.getX().accept(opPrinter);
			builder.append(opSymbol);
			node.getY().accept(opPrinter);
			if(paren)
				builder.append(")");
		}

		@Override
		public String visit(Zero node) {
			return "0";
		}

		@Override
		public String visit(One node) {
			return "1";
		}

		@Override
		public String visit(Not node) {
			builder.append("!");
			node.getX().accept(suite.NotPrinter);
			if(level>suite.NotPrinter.level)
				throw new RuntimeException("o_O");
			return null;
		}

		@Override
		public Object visit(Imply node) {
			visitBinary(suite.ImplyPrinter.level, suite.OrPrinter, " ->", node);
			return null;
		}

		@Override
		public String visit(FreeVariable variable) {
			String label = variable.getLabel();
			FreeVariable nameHolder = varMap.get(label);
			if(nameHolder == null)
				varMap.put(label, variable);
			else
				if(nameHolder != variable)
					throw new RuntimeException("name conflict! duplicate name " + label);

			builder.append(label);

			return null;
		}

	/*	Map<FreeVariable, Integer> varIndices = new HashMap<FreeVariable, Integer>();
		int varCounter = 0;
		private int getVarIndex(FreeVariable variable) {
			Integer stored = varIndices.get(variable);
			if(stored != null)
				return stored;
			int next = varCounter++;
			varIndices.put(variable, next);
			return next;
		}*/

		@Override
		public String visit(Or node) {
			visitBinary(suite.OrPrinter.level, suite.OrPrinter, "|", node);
			return null;
		}
	}

	public static String toString(BooleanFormula f) {
		Printer printer = getPrinter();
		f.accept(printer);
		return printer.builder.toString();
	}

	private static Printer getPrinter() {
		return new PrinterSuite().IffPrinter;
	}

	Printer printer;

	@Override
	public String visit(And node) {
		return toString(node);
	}

	@Override
	public String visit(Iff node) {
		return toString(node);
	}

	@Override
	public String visit(Zero node) {
		return toString(node);
	}

	@Override
	public String visit(One node) {
		return toString(node);
	}

	@Override
	public String visit(Not node) {
		return toString(node);
	}

	@Override
	public String visit(Imply node) {
		return toString(node);
	}

	@Override
	public String visit(FreeVariable node) {
		return toString(node);
	}

	@Override
	public String visit(Or node) {
		return toString(node);
	}
}
