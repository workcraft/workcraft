package org.workcraft.formula;

import static org.workcraft.formula.BooleanOperations.and;
import static org.workcraft.formula.BooleanOperations.or;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.cnf.HumanReadableCnfPrinter;
import org.workcraft.formula.sat.CleverCnfGenerator;

public class CleverCnfGeneratorTests {

    @Test
    public void testHumanReadableCnfPrinter() {
        CleverCnfGenerator gen = new CleverCnfGenerator();
        BooleanVariable var1 = new FreeVariable("x");
        BooleanVariable var2 = new FreeVariable("y");
        // cnf = x * y + y * x + x * y
        Cnf cnf = gen.generateCnf(or(or(and(var1, var2), and(var2, var1)), and(var1, var2)));
        String str = cnf.toString(new HumanReadableCnfPrinter());
        Assert.assertEquals("x \ny \n", str);
    }

}
