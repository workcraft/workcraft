package org.workcraft.plugins.cpog.optimisation.tests;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.CpogEncoding;
import org.workcraft.plugins.cpog.optimisation.LegacyDefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.OneHotNumberProvider;
import org.workcraft.plugins.cpog.optimisation.Optimiser;

public class MaxCpogTest {
    static String [] cpog =
    {
            "-0001",
            "00011",
            "11111",
            "10111",
            "1z1ZZ"
    };

    @Test
    public void test1() {
        Optimiser<OneHotIntBooleanFormula> optimiser = new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider(), null);
        LegacyDefaultCpogSolver<BooleanFormula> solver = new LegacyDefaultCpogSolver<BooleanFormula>(optimiser, new CleverCnfGenerator());
        CpogEncoding result = solver.solve(cpog, 3, 4);
        Assert.assertNotNull("Should be satisfiable", result);
/*        for(BooleanFormula formula : result.getFunctions()) {
            System.out.println(FormulaToString.toString(formula));
        }
        for(boolean[] enc : result.getEncoding()) {
            for(boolean b : enc) {
                System.out.print(b?"1":"0");
            }
            System.out.println();
        }*/
    }
}
