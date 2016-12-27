package org.workcraft.testing.formula.sat;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.onehot.OneHotIntBooleanFormula;
import org.workcraft.formula.encoding.onehot.OneHotNumberProvider;
import org.workcraft.formula.sat.CleverCnfGenerator;
import org.workcraft.formula.sat.LegacySolver;
import org.workcraft.formula.sat.Optimiser;

public class OneHotSolverTests extends SolverTests {

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
