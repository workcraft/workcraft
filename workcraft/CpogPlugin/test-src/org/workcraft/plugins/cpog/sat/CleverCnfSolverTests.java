package org.workcraft.plugins.cpog.sat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.encoding.onehot.CnfGeneratingOptimiser;

// Test methods are defined in the base SolverTests class
@SuppressWarnings("PMD.TestClassWithoutTestCases")
// This only works with MINISAT solver which is not supported in Travis OSX
@Disabled
class CleverCnfSolverTests extends SolverTests {

    @BeforeAll
    static void setSatSolver() {
        CpogSettings.setSatSolver(CpogSettings.SatSolver.MINISAT);
    }

    @Override
    protected LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<>(
                new CnfGeneratingOptimiser(),
                new CleverCnfGenerator());
    }

}
