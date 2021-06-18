package org.workcraft.plugins.cpog.sat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.encoding.onehot.CnfGeneratingOptimiser;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;

@Disabled // This only works with MINISAT solver which is not supported in Travis OSX
class CnfSolverTests extends SolverTests {

    @BeforeAll
    static void setSatSolver() {
        if (DesktopApi.getOs().isWindows()) {
            CpogSettings.setClaspCommand(BackendUtils.getTemplateToolPath("clasp", "clasp"));
            CpogSettings.setMinisatCommand(BackendUtils.getTemplateToolPath("minisat", "minisat"));
        }
        CpogSettings.setSatSolver(CpogSettings.SatSolver.MINISAT);
    }

    @Override
    protected LegacySolver<Cnf> createSolver() {
        return new LegacySolver<>(
                new CnfGeneratingOptimiser(),
                new SimpleCnfTaskProvider());
    }

}
