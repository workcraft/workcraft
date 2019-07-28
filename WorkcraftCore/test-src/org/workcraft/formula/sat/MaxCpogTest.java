package org.workcraft.formula.sat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.Encoding;
import org.workcraft.formula.encoding.onehot.OneHotIntBooleanFormula;
import org.workcraft.formula.encoding.onehot.OneHotNumberProvider;
import org.workcraft.plugins.builtin.settings.CommonSatSettings;
import org.workcraft.plugins.builtin.settings.CommonSatSettings.SatSolver;
import org.workcraft.utils.DesktopApi;

public class MaxCpogTest {

    static String[] cpog = {"-0001", "00011", "11111", "10111", "1z1ZZ"};

    @BeforeClass
    public static void skipOnWindows() {
        Assume.assumeFalse(DesktopApi.getOs().isWindows());
    }

    @BeforeClass
    public static void setSatSolver() {
        CommonSatSettings.setSatSolver(SatSolver.CLASP);
    }

    @Test
    public void testCpogEncoding() {
        Optimiser<OneHotIntBooleanFormula> optimiser = new Optimiser<>(new OneHotNumberProvider(), null);
        LegacySolver<BooleanFormula> solver = new LegacySolver<>(optimiser, new CleverCnfGenerator());
        Encoding result = solver.solve(cpog, 3, 4);
        Assert.assertNotNull("Should be satisfiable", result);
    }

}
