package org.workcraft.testing.formula.sat;

import org.junit.BeforeClass;
import org.workcraft.formula.BinaryIntBooleanFormula;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.BinaryNumberProvider;
import org.workcraft.formula.sat.CleverCnfGenerator;
import org.workcraft.formula.sat.LegacySolver;
import org.workcraft.formula.sat.Optimiser;
import org.workcraft.plugins.shared.CommonSatSettings;
import org.workcraft.plugins.shared.CommonSatSettings.SatSolver;

public class BinarySolverTests extends SolverTests {

    @BeforeClass
    public static void setSatSolver() {
        CommonSatSettings.setSatSolver(SatSolver.CLASP);
    }

    @Override
    protected LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<BooleanFormula>(
                new Optimiser<BinaryIntBooleanFormula>(new BinaryNumberProvider()),
                new CleverCnfGenerator());
    }

}
