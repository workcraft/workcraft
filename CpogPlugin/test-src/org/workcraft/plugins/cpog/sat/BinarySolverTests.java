package org.workcraft.plugins.cpog.sat;

import org.junit.BeforeClass;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.encoding.BinaryNumberProvider;

public class BinarySolverTests extends SolverTests {

    @BeforeClass
    public static void setSatSolver() {
        CpogSettings.setSatSolver(CpogSettings.SatSolver.CLASP);
    }

    @Override
    protected LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<>(
                new Optimiser<>(new BinaryNumberProvider()),
                new CleverCnfGenerator());
    }

}
