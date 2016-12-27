package org.workcraft.testing.formula.sat;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.onehot.CnfGeneratingOptimiser;
import org.workcraft.formula.sat.CleverCnfGenerator;
import org.workcraft.formula.sat.LegacySolver;
import org.workcraft.plugins.shared.CommonSatSettings;
import org.workcraft.plugins.shared.CommonSatSettings.SatSolver;

@Ignore // This only works with MINISAT solver which is not supported in Travis OSX
public class CleverCnfSolverTests extends SolverTests {

    @BeforeClass
    public static void setSatSolver() {
        CommonSatSettings.setSatSolver(SatSolver.MINISAT);
    }

    @Override
    protected LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<BooleanFormula>(
                new CnfGeneratingOptimiser(),
                new CleverCnfGenerator());
    }

}
