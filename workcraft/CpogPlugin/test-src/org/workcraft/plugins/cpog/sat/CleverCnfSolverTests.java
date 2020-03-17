package org.workcraft.plugins.cpog.sat;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.encoding.onehot.CnfGeneratingOptimiser;

@Ignore // This only works with MINISAT solver which is not supported in Travis OSX
public class CleverCnfSolverTests extends SolverTests {

    @BeforeClass
    public static void setSatSolver() {
        CpogSettings.setSatSolver(CpogSettings.SatSolver.MINISAT);
    }

    @Override
    protected LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<BooleanFormula>(
                new CnfGeneratingOptimiser(),
                new CleverCnfGenerator());
    }

}
