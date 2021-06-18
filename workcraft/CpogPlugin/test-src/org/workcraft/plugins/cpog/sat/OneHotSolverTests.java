package org.workcraft.plugins.cpog.sat;

import org.junit.jupiter.api.BeforeAll;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.encoding.onehot.OneHotNumberProvider;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;

class OneHotSolverTests extends SolverTests {

    @BeforeAll
    static void setSatSolver() {
        if (DesktopApi.getOs().isWindows()) {
            CpogSettings.setClaspCommand(BackendUtils.getTemplateToolPath("clasp", "clasp"));
            CpogSettings.setMinisatCommand(BackendUtils.getTemplateToolPath("minisat", "minisat"));
        }
        CpogSettings.setSatSolver(CpogSettings.SatSolver.CLASP);
    }

    @Override
    protected LegacySolver<BooleanFormula> createSolver() {
        return new LegacySolver<>(
                new Optimiser<>(new OneHotNumberProvider()),
                new CleverCnfGenerator());
    }

    @Override
    protected LegacySolver<BooleanFormula> createSolver(int[] levels) {
        return new LegacySolver<>(
                new Optimiser<>(new OneHotNumberProvider(), levels),
                new CleverCnfGenerator());
    }

}
