package org.workcraft.plugins.cpog.sat;

import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanReplacer;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.plugins.cpog.formula.MemoryConservingBooleanWorker;
import org.workcraft.plugins.cpog.formula.PrettifyBooleanWorker;

import java.util.HashMap;
import java.util.Map;

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
