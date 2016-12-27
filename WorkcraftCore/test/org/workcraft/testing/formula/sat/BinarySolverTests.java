package org.workcraft.testing.formula.sat;

import org.junit.Ignore;
import org.workcraft.formula.BinaryIntBooleanFormula;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.BinaryNumberProvider;
import org.workcraft.formula.sat.CleverCnfGenerator;
import org.workcraft.formula.sat.LegacySolver;
import org.workcraft.formula.sat.Optimiser;

@Ignore
public class BinarySolverTests extends SolverTests {

    @Override
    protected LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<BooleanFormula>(
                new Optimiser<BinaryIntBooleanFormula>(new BinaryNumberProvider()),
                new CleverCnfGenerator());
    }

}
