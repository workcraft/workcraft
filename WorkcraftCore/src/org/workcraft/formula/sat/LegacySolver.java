package org.workcraft.formula.sat;

import org.workcraft.formula.BooleanSolution;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FreeVariable;
import org.workcraft.formula.cnf.CnfGenerator;
import org.workcraft.formula.encoding.Encoding;

public class LegacySolver<T> {
    private final SatProblemGenerator<? extends T> problemGenerator;
    private final CnfGenerator<? super T> cnfConverter;

    public LegacySolver(SatProblemGenerator<? extends T> problemGenerator,
            CnfGenerator<? super T> simpleCnfTaskProvider) {
        this.problemGenerator = problemGenerator;
        this.cnfConverter = simpleCnfTaskProvider;
    }

    public Encoding solve(String[] scenarios, int freeVars, int derivedVars) {

        BooleanVariable[] vars = new BooleanVariable[freeVars];

        char nextVar = 'z';
        for (int i = 0; i < freeVars; i++) {
            vars[i] = new FreeVariable("" + nextVar);
            nextVar--;
        }

        OptimisationTask<? extends T> task = problemGenerator.getFormula(scenarios, vars, derivedVars);

        BooleanSolution solution = new ConsoleBooleanSolver().solve(cnfConverter.getCnf(task.getTask()));
        return SolutionPrettifier.prettifySolution(task, solution);
    }
}
