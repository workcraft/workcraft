package org.workcraft.plugins.cpog.optimisation;

public class DefaultCpogSolver<T>
{
	private final CpogSATProblemGenerator<? extends T> problemGenerator;
	private final RawCnfGenerator<? super T> cnfConverter;

	public DefaultCpogSolver(CpogSATProblemGenerator<? extends T> problemGenerator,
			RawCnfGenerator<? super T> simpleCnfTaskProvider) {
				this.problemGenerator = problemGenerator;
				this.cnfConverter = simpleCnfTaskProvider;
	}

	public CpogEncoding solve(String[] scenarios, BooleanVariable [] variables, int derivedVars)
	{
		CpogOptimisationTask<? extends T> task = problemGenerator.getFormula(scenarios, variables, derivedVars);

		BooleanSolution solution = new ConsoleBooleanSolver().solve(cnfConverter.getCnf(task.getTask()));
		return SolutionPrettifier.prettifySolution(task, solution);
	}
}
