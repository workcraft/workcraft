package org.workcraft.formula.sat;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.workcraft.formula.encoding.Encoding;
import org.workcraft.plugins.shared.CommonSatSettings;
import org.workcraft.plugins.shared.CommonSatSettings.SatSolver;

public abstract class SolverTests {

    static String[] smallScenarios = {"110", "101", "011"};

    static String[] xorScenarios = {"000", "011", "101", "110"};

    protected abstract LegacySolver<?> createSolver();

    protected LegacySolver<?> createSolver(int[] levels) {
        return null;
    }

    @BeforeClass
    public static void setSatSolver() {
        CommonSatSettings.setSatSolver(SatSolver.MINISAT);
    }

    @Test
    public void testSmall10() {
        Encoding solution = createSolver().solve(smallScenarios, 1, 0);
        assertNull(solution);
    }

    @Ignore @Test
    public void testSmall20() {
        Encoding solution = createSolver().solve(smallScenarios, 2, 0);
        assertNull(solution);
    }

    @Test
    public void testSmall21() {
        Encoding solution = createSolver().solve(smallScenarios, 2, 1);
        assertNotNull(solution);
    }

    @Test
    public void testSmall22() {
        Encoding solution = createSolver().solve(smallScenarios, 2, 2);
        assertNotNull(solution);
    }

    @Test
    public void testSmall23() {
        Encoding solution = createSolver().solve(smallScenarios, 2, 3);
        assertNotNull(solution);
    }

    @Test
    public void testSmall30() {
        Encoding solution = createSolver().solve(smallScenarios, 3, 0);
        assertNotNull(solution);
    }

    @Test
    public void testXor() {
        Encoding solution = createSolver().solve(xorScenarios, 2, 1);
        assertNull(solution);
    }

}
