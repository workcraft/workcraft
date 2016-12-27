package org.workcraft.plugins.cpog.optimisation.tests;

import org.workcraft.plugins.cpog.optimisation.Cnf;
import org.workcraft.plugins.cpog.optimisation.CnfGeneratingOptimiser;
import org.workcraft.plugins.cpog.optimisation.LegacyCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LegacyDefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.SimpleCnfTaskProvider;

public class CnfSolverTests extends SolverTests {

    public CnfSolverTests() {
    }

    protected LegacyCpogSolver createSolver() {
        return new LegacyDefaultCpogSolver<Cnf>(new CnfGeneratingOptimiser(), new SimpleCnfTaskProvider());
    }

}
