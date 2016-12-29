package org.workcraft.formula.sat;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.encoding.onehot.CnfGeneratingOptimiser;
import org.workcraft.plugins.shared.CommonSatSettings;
import org.workcraft.plugins.shared.CommonSatSettings.SatSolver;

@Ignore // This only works with MINISAT solver which is not supported in Travis OSX
public class CnfSolverTests extends SolverTests {

    @BeforeClass
    public static void setSatSolver() {
        CommonSatSettings.setSatSolver(SatSolver.MINISAT);
    }

    @Override
    protected LegacySolver<Cnf> createSolver() {
        return new LegacySolver<Cnf>(
                new CnfGeneratingOptimiser(),
                new SimpleCnfTaskProvider());
    }

}
