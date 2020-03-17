package org.workcraft.plugins.cpog.sat;

import org.junit.BeforeClass;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.encoding.onehot.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.encoding.onehot.OneHotNumberProvider;

public class OneHotSolverTests extends SolverTests {

    @BeforeClass
    public static void setSatSolver() {
        CpogSettings.setSatSolver(CpogSettings.SatSolver.CLASP);
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
