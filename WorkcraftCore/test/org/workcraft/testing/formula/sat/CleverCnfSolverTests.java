package org.workcraft.testing.formula.sat;

import org.junit.Ignore;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.onehot.CnfGeneratingOptimiser;
import org.workcraft.formula.sat.CleverCnfGenerator;
import org.workcraft.formula.sat.LegacySolver;

@Ignore
public class CleverCnfSolverTests extends SolverTests {

    @Override
    protected LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<BooleanFormula>(
                new CnfGeneratingOptimiser(),
                new CleverCnfGenerator());
    }

}
