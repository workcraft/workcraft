package org.workcraft.plugins.cpog.optimisation.tests;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.CnfGeneratingOptimiser;
import org.workcraft.plugins.cpog.optimisation.LegacyCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LegacyDefaultCpogSolver;

public class CleverCnfGeneratorCnfSolverTests extends SolverTests {

    public CleverCnfGeneratorCnfSolverTests() {
    }

    protected LegacyCpogSolver createSolver() {
        return new LegacyDefaultCpogSolver<BooleanFormula>(
                new CnfGeneratingOptimiser(), new CleverCnfGenerator());
    }

}
