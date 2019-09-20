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
            BooleanFormula value = solution.getSolution(var) ? One.getInstance() : Zero.getInstance();
            result.put(var, value);
        }
        return result;
    }

}
