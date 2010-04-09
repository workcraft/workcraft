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
package org.workcraft.plugins.cpog.optimisation.tests;

import org.junit.Test;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.FreeVariable;
import org.workcraft.plugins.cpog.optimisation.HumanReadableCnfPrinter;

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.*;

public class CleverCnfGeneratorTests {
	@Test
	public void qwe()
	{
		CleverCnfGenerator gen = new CleverCnfGenerator();

		FreeVariable var1 = new FreeVariable("x");
		FreeVariable var2 = new FreeVariable("y");
		FreeVariable var3 = new FreeVariable("z");
		System.out.println(gen.generateCnf(or(or(and(var1, var2), and(var2, var1)), and(var1, var2))).toString(new HumanReadableCnfPrinter()));
	}
}
