package org.workcraft.plugins.cpog.optimisation.tests;

import org.junit.Test;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.FreeVariable;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.HumanReadableCnfPrinter;

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.*;

public class CleverCnfGeneratorTests {

    @Test
    public void qwe() {
        CleverCnfGenerator gen = new CleverCnfGenerator();
        BooleanVariable var1 = new FreeVariable("x");
        BooleanVariable var2 = new FreeVariable("y");
        System.out.println(gen.generateCnf(or(or(and(var1, var2), and(var2, var1)), and(var1, var2))).toString(new HumanReadableCnfPrinter()));
    }

}
