package org.workcraft.plugins.cpog.sat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.encoding.Encoding;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;

abstract class SolverTests {

    private static final String[] smallScenarios = {"110", "101", "011"};
    private static final String[] xorScenarios = {"000", "011", "101", "110"};

    protected abstract LegacySolver<?> createSolver();

    protected LegacySolver<?> createSolver(int[] levels) {
        return null;
    }

    @BeforeAll
    static void setSatSolver() {
        if (DesktopApi.getOs().isWindows()) {
            CpogSettings.setClaspCommand(BackendUtils.getTemplateToolPath("clasp", "clasp"));
            CpogSettings.setMinisatCommand(BackendUtils.getTemplateToolPath("minisat", "minisat"));
        }
        CpogSettings.setSatSolver(CpogSettings.SatSolver.MINISAT);
    }

    @Test
    void testSmall10() {
        Encoding solution = createSolver().solve(smallScenarios, 1, 0);
        Assertions.assertNull(solution);
    }

    @Disabled
    @Test
    void testSmall20() {
        Encoding solution = createSolver().solve(smallScenarios, 2, 0);
        Assertions.assertNull(solution);
    }

    @Test
    void testSmall21() {
        Encoding solution = createSolver().solve(smallScenarios, 2, 1);
        Assertions.assertNotNull(solution);
    }

    @Test
    void testSmall22() {
        Encoding solution = createSolver().solve(smallScenarios, 2, 2);
        Assertions.assertNotNull(solution);
    }

    @Test
    void testSmall23() {
        Encoding solution = createSolver().solve(smallScenarios, 2, 3);
        Assertions.assertNotNull(solution);
    }

    @Test
    void testSmall30() {
        Encoding solution = createSolver().solve(smallScenarios, 3, 0);
        Assertions.assertNotNull(solution);
    }

    @Test
    void testXor() {
        Encoding solution = createSolver().solve(xorScenarios, 2, 1);
        Assertions.assertNull(solution);
    }

}
