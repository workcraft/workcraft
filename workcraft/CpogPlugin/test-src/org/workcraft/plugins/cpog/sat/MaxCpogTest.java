package org.workcraft.plugins.cpog.sat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.encoding.Encoding;
import org.workcraft.plugins.cpog.encoding.onehot.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.encoding.onehot.OneHotNumberProvider;
import org.workcraft.utils.DesktopApi;

public class MaxCpogTest {

    private static final String[] cpog = {"-0001", "00011", "11111", "10111", "1z1ZZ"};

    @BeforeAll
    public static void skipOnWindows() {
        Assumptions.assumeFalse(DesktopApi.getOs().isWindows());
    }

    @BeforeAll
    public static void setSatSolver() {
        CpogSettings.setSatSolver(CpogSettings.SatSolver.CLASP);
    }

    @Test
    public void testCpogEncoding() {
        Optimiser<OneHotIntBooleanFormula> optimiser = new Optimiser<>(new OneHotNumberProvider(), null);
        LegacySolver<BooleanFormula> solver = new LegacySolver<>(optimiser, new CleverCnfGenerator());
        Encoding result = solver.solve(cpog, 3, 4);
        Assertions.assertNotNull(result, "Should be satisfiable");
    }

}
