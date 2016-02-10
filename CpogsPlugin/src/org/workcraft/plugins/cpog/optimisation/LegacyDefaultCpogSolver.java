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

public class LegacyDefaultCpogSolver<T> implements LegacyCpogSolver {
    private final CpogSATProblemGenerator<? extends T> problemGenerator;
    private final RawCnfGenerator<? super T> cnfConverter;

    public LegacyDefaultCpogSolver(CpogSATProblemGenerator<? extends T> problemGenerator,
            RawCnfGenerator<? super T> simpleCnfTaskProvider) {
                this.problemGenerator = problemGenerator;
                this.cnfConverter = simpleCnfTaskProvider;
    }

    @Override
    public CpogEncoding solve(String[] scenarios, int freeVars, int derivedVars) {

        BooleanVariable[] vars = new BooleanVariable[freeVars];

        char nextVar = 'z';
        for(int i = 0; i < freeVars; i++) {
            vars[i] = new FreeVariable("" + nextVar);
            nextVar--;
        }

        CpogOptimisationTask<? extends T> task = problemGenerator.getFormula(scenarios, vars, derivedVars);

        BooleanSolution solution = new ConsoleBooleanSolver().solve(cnfConverter.getCnf(task.getTask()));
        return SolutionPrettifier.prettifySolution(task, solution);
    }
}
