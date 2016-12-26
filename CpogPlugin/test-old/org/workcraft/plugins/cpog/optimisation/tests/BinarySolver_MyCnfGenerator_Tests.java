package org.workcraft.plugins.cpog.optimisation.tests;

import org.workcraft.plugins.cpog.optimisation.BinaryIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BinaryNumberProvider;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.LegacyCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LegacyDefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.Optimiser;

public class BinarySolver_MyCnfGenerator_Tests extends SolverTests {

    public BinarySolver_MyCnfGenerator_Tests() {
    }

    protected LegacyCpogSolver createSolver() {
        return new LegacyDefaultCpogSolver<BooleanFormula>(
                new Optimiser<BinaryIntBooleanFormula>(new BinaryNumberProvider()),
                new CleverCnfGenerator());
    }

}
