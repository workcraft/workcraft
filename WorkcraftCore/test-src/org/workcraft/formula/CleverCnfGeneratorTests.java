package org.workcraft.formula;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.cnf.SimpleCnfPrinter;
import org.workcraft.formula.sat.CleverCnfGenerator;

import static org.workcraft.formula.BooleanOperations.and;
import static org.workcraft.formula.BooleanOperations.or;

public class CleverCnfGeneratorTests {

    @Test
    public void testSimpleCnfPrinter() {
        CleverCnfGenerator generator = new CleverCnfGenerator();
        BooleanVariable var1 = new FreeVariable("x");
        BooleanVariable var2 = new FreeVariable("y");
        // cnf = x * y + y * x + x * y
        Cnf cnf = generator.generate(or(or(and(var1, var2), and(var2, var1)), and(var1, var2)));
        String str = cnf.toString(new SimpleCnfPrinter());
        Assert.assertEquals("x \ny \n", str);
    }

}
