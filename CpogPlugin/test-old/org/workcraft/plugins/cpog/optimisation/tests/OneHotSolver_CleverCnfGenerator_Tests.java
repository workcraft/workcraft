package org.workcraft.plugins.cpog.optimisation.tests;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.LegacyCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LegacyDefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.OneHotNumberProvider;
import org.workcraft.plugins.cpog.optimisation.Optimiser;

public class OneHotSolver_CleverCnfGenerator_Tests extends SolverTests {

    public OneHotSolver_CleverCnfGenerator_Tests() {
    }

    protected LegacyCpogSolver createSolver() {
        return new LegacyDefaultCpogSolver<BooleanFormula>(
                new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider()),
                new CleverCnfGenerator());
    }

    @Override
    protected LegacyCpogSolver createSolver(int[] levels) {
        return new LegacyDefaultCpogSolver<BooleanFormula>(
                new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider(), levels),
                new CleverCnfGenerator());
    }

}
