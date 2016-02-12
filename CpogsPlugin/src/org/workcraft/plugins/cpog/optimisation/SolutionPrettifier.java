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

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.SolutionSubstitutor;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

public class SolutionPrettifier {
    public static <T> CpogEncoding prettifySolution(CpogOptimisationTask<T> task, BooleanSolution solution) {
        if(solution==null)
            return null;

        BooleanFormula[][] encodingVars = task.getEncodingVars();
        BooleanFormula[] functionVars = task.getFunctionVars();
        if(functionVars == null)
            throw new RuntimeException("functionVars is null");
        if(encodingVars == null)
            throw new RuntimeException("encodingVars is null");

        BooleanVisitor<BooleanFormula> substitutor = new SolutionSubstitutor(solution);
        BooleanVisitor<Boolean> evaluator = new BooleanEvaluator();

        BooleanFormula[] functions = new BooleanFormula[functionVars.length];

        for(int i=0; i<functions.length; i++)
            functions[i] = functionVars[i].accept(substitutor);

        boolean[][] encoding = new boolean[encodingVars.length][];
        for(int i=0; i<encodingVars.length; i++) {
            encoding[i] = new boolean[encodingVars[i].length];
            for(int j=0; j<encodingVars[i].length; j++)
                encoding[i][j] = encodingVars[i][j].accept(substitutor).accept(evaluator);
        }

        return new CpogEncoding(encoding, functions);
    }
}
