package org.workcraft.testing.formula.sat;

import org.junit.Ignore;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.encoding.onehot.CnfGeneratingOptimiser;
import org.workcraft.formula.sat.LegacySolver;
import org.workcraft.formula.sat.SimpleCnfTaskProvider;

@Ignore
public class CnfSolverTests extends SolverTests {

    @Override
    protected LegacySolver<Cnf> createSolver() {
        return new LegacySolver<Cnf>(
                new CnfGeneratingOptimiser(),
                new SimpleCnfTaskProvider());
    }

}
