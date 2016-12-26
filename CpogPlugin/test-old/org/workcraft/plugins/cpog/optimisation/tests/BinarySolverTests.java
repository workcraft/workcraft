package org.workcraft.plugins.cpog.optimisation.tests;

import org.workcraft.plugins.cpog.optimisation.BinaryIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BinaryNumberProvider;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.LegacyCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LegacyDefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LimBooleCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.Optimiser;

public class BinarySolverTests extends SolverTests {

    public BinarySolverTests() {
    }

    protected LegacyCpogSolver createSolver() {
        return new LegacyDefaultCpogSolver<BooleanFormula>(
                new Optimiser<BinaryIntBooleanFormula>(new BinaryNumberProvider()),
                new LimBooleCnfGenerator());
    }

}
