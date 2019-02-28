package org.workcraft.formula.sat;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.onehot.CnfGeneratingOptimiser;
import org.workcraft.plugins.builtin.settings.CommonSatSettings;
import org.workcraft.plugins.builtin.settings.CommonSatSettings.SatSolver;

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
