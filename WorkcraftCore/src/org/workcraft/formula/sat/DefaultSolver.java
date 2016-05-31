package org.workcraft.formula.sat;

import org.workcraft.formula.BooleanSolution;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.cnf.RawCnfGenerator;
import org.workcraft.formula.encoding.Encoding;

public class DefaultSolver<T> {
    private final SatProblemGenerator<? extends T> problemGenerator;
    private final RawCnfGenerator<? super T> cnfConverter;

    public DefaultSolver(SatProblemGenerator<? extends T> problemGenerator,
            RawCnfGenerator<? super T> simpleCnfTaskProvider) {
        this.problemGenerator = problemGenerator;
        this.cnfConverter = simpleCnfTaskProvider;
    }

    public OptimisationTask<? extends T> getTask(String[] scenarios, BooleanVariable[] variables, int derivedVars) {
        return problemGenerator.getFormula(scenarios, variables, derivedVars);
    }

    public Encoding solve(String[] scenarios, BooleanVariable[] variables, int derivedVars) {
        OptimisationTask<? extends T> task = problemGenerator.getFormula(scenarios, variables, derivedVars);

        BooleanSolution solution = new ConsoleBooleanSolver().solve(cnfConverter.getCnf(task.getTask()));
        return SolutionPrettifier.prettifySolution(task, solution);
    }
}
