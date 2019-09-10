package org.workcraft.plugins.cpog.sat;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.encoding.onehot.CnfGeneratingOptimiser;

@Ignore // This only works with MINISAT solver which is not supported in Travis OSX
public class CnfSolverTests extends SolverTests {

    @BeforeClass
    public static void setSatSolver() {
        CpogSettings.setSatSolver(CpogSettings.SatSolver.MINISAT);
    }

    @Override
    protected LegacySolver<Cnf> createSolver() {
        return new LegacySolver<Cnf>(
                new CnfGeneratingOptimiser(),
                new SimpleCnfTaskProvider());
    }

}
