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
/*
public class Snippet {
	BooleanFormula getFormula(String [] scenarios, String [] forcedVars, int freeVariables, int derivedVariables)
	{

		int nonDerivedVariables = freeVariables;
		int forcedVarCount = 0;
		if(forcedVars != null)
		{
			if(forcedVars.length != scenarios.length)
				throw new RuntimeException("Forced vars length should be the same as scenarios length");
			forcedVarCount = forcedVars[0].length();
		}
		nonDerivedVariables += forcedVarCount;

		//Generate all possible encodings...
		BooleanFormula [][] encodings = new BooleanFormula [scenarios.length][];
		for(int i=0;i<scenarios.length;i++)
		{
			encodings[i] = new BooleanFormula[nonDerivedVariables];
			if(i == 0)
				for(int j=0;j<freeVariables;j++)
					encodings[i][j] = Zero.instance();
			else
				for(int j=0;j<freeVariables;j++)
					encodings[i][j] = new FV("x"+j+"_s"+i);
			for(int j=0;j<forcedVarCount;j++) {
				encodings[i][freeVariables+j] = parseBoolean(forcedVars[i].charAt(j));
			}
		}

		//... and all possible functions.
		AndFunction [] derivedFunctions = new AndFunction[derivedVariables];
		for(int i=0;i<derivedVariables;i++)
			derivedFunctions[i] = generateBinaryFunction(nonDerivedVariables, i);

		//Evaluate all functions for all scenarios.
		BooleanFormula [][] functionSpace = new BooleanFormula [scenarios.length][];
		int totalVariables = nonDerivedVariables*2 + derivedVariables*2;
		for(int i=0;i<scenarios.length;i++)
		{
			functionSpace[i] = new BooleanFormula[totalVariables];
			for(int j=0;j<nonDerivedVariables;j++)
			{
				functionSpace[i][j*2] = encodings[i][j];
				functionSpace[i][j*2+1] = not(encodings[i][j]);
			}
			for(int j=0;j<derivedVariables;j++)
			{
				int jj = j+nonDerivedVariables;
				BooleanFormula eval = evaluate(derivedFunctions[j], encodings[i]);
				functionSpace[i][jj*2] = eval;
				functionSpace[i][jj*2+1] = not(eval);
			}
		}

		int functionCount = scenarios[0].length();

		List<BooleanFormula> tableConditions = new ArrayList<BooleanFormula>();

		//Try to match CPOG functions with generated functions.
		for(int i=0;i<functionCount;i++)
		{
			OneHotIntBooleanFormula varId = generateInt("cpog_f"+i+"_",totalVariables);
			for(int j=0;j<scenarios.length;j++)
			{
				BooleanFormula value = select(functionSpace[j], varId);

				char ch = scenarios[j].charAt(i);
				if(ch=='-')
					continue;
				else if(ch=='1')
					tableConditions.add(value);
				else if(ch=='0')
					tableConditions.add(not(value));
				else throw new RuntimeException("unknown symbol: " + parseBoolean(ch));
			}

		}

		tableConditions.addAll(rho);

		return and(tableConditions);
	}

}

*/