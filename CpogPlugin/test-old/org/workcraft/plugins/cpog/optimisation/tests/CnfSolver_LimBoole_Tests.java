package org.workcraft.plugins.cpog.optimisation.tests;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CnfGeneratingOptimiser;
import org.workcraft.plugins.cpog.optimisation.LegacyCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LegacyDefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LimBooleCnfGenerator;

public class CnfSolver_LimBoole_Tests extends SolverTests {

    public CnfSolver_LimBoole_Tests() {
    }

    protected LegacyCpogSolver createSolver() {
        return new LegacyDefaultCpogSolver<BooleanFormula>(new CnfGeneratingOptimiser(), new LimBooleCnfGenerator());
    }

}
