package org.workcraft.testing.formula.sat;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.encoding.Encoding;
import org.workcraft.formula.encoding.onehot.OneHotIntBooleanFormula;
import org.workcraft.formula.encoding.onehot.OneHotNumberProvider;
import org.workcraft.formula.sat.CleverCnfGenerator;
import org.workcraft.formula.sat.LegacySolver;
import org.workcraft.formula.sat.Optimiser;
import org.workcraft.plugins.shared.CommonSatSettings;
import org.workcraft.plugins.shared.CommonSatSettings.SatSolver;

public class MaxCpogTest {

    static String[] cpog = {"-0001", "00011", "11111", "10111", "1z1ZZ"};

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
