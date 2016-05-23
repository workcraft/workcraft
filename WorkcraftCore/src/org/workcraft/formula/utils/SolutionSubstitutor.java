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
package org.workcraft.formula.utils;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanSolution;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.BooleanWorker;
import org.workcraft.formula.MemoryConservingBooleanWorker;
import org.workcraft.formula.One;
import org.workcraft.formula.PrettifyBooleanWorker;
import org.workcraft.formula.Zero;

public class SolutionSubstitutor extends BooleanReplacer {

    private static final BooleanWorker WORKER = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());

    public SolutionSubstitutor(BooleanSolution solution) {
        super(buildMap(solution), WORKER);
    }

    private static Map<BooleanVariable, BooleanFormula> buildMap(BooleanSolution solution) {
        Map<BooleanVariable, BooleanFormula> result = new HashMap<>();
        for (BooleanVariable var : solution.getVariables()) {
            BooleanFormula value = solution.getSolution(var) ? One.instance() : Zero.instance();
            result.put(var, value);
        }
        return result;
    }

}
