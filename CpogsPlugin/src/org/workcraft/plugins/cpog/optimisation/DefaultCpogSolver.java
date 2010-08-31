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

public class DefaultCpogSolver<T> implements CpogSolver
{
	private final CpogSATProblemGenerator<? extends T> problemGenerator;
	private final RawCnfGenerator<? super T> cnfConverter;

	public DefaultCpogSolver(CpogSATProblemGenerator<? extends T> problemGenerator,
			RawCnfGenerator<? super T> simpleCnfTaskProvider) {
				this.problemGenerator = problemGenerator;
				this.cnfConverter = simpleCnfTaskProvider;
	}

	@Override
	public CpogEncoding solve(String[] scenarios, int freeVars, int derivedVars) {
		CpogOptimisationTask<? extends T> task = problemGenerator.getFormula(scenarios, freeVars, derivedVars);

		BooleanSolution solution = new MiniSatBooleanSolver().solve(cnfConverter.getCnf(task.getTask()));
		return SolutionPrettifier.prettifySolution(task, solution);
	}
}
