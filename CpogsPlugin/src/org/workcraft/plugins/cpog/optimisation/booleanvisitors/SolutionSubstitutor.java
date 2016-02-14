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

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanSolution;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.MemoryConservingBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.PrettifyBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class SolutionSubstitutor extends BooleanReplacer {

    private static final BooleanWorker WORKER = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());

    public SolutionSubstitutor(BooleanSolution solution) {
        super(buildMap(solution), WORKER);
    }

    private static Map<BooleanVariable, BooleanFormula> buildMap(BooleanSolution solution) {
        Map<BooleanVariable, BooleanFormula> result = new HashMap<BooleanVariable, BooleanFormula>();
        for (BooleanVariable var : solution.getVariables()) {
            BooleanFormula value = solution.getSolution(var) ? One.instance() : Zero.instance();
            result.put(var, value);
        }
        return result;
    }

}
