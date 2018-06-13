package org.workcraft.formula.sat;

import org.junit.BeforeClass;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.onehot.OneHotIntBooleanFormula;
import org.workcraft.formula.encoding.onehot.OneHotNumberProvider;
import org.workcraft.plugins.shared.CommonSatSettings;
import org.workcraft.plugins.shared.CommonSatSettings.SatSolver;

public class OneHotSolverTests extends SolverTests {

    @BeforeClass
    public static void setSatSolver() {
        CommonSatSettings.setSatSolver(SatSolver.CLASP);
    }

    @Override
    protected LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<BooleanFormula>(
                new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider()),
                new CleverCnfGenerator());
    }

    @Override
    protected LegacySolver<BooleanFormula> createSolver(int[] levels) {
        return new LegacySolver<BooleanFormula>(
                new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider(), levels),
                new CleverCnfGenerator());
    }

}
