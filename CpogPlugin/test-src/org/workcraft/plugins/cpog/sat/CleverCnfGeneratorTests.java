package org.workcraft.plugins.cpog.sat;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FreeVariable;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.utils.ClauseUtils;
import org.workcraft.utils.SetUtils;

import java.util.Set;

import static org.workcraft.formula.BooleanOperations.and;
import static org.workcraft.formula.BooleanOperations.or;

public class CleverCnfGeneratorTests {

    @Test
    public void testSimpleCnfPrinter() {
        CleverCnfGenerator generator = new CleverCnfGenerator();
        BooleanVariable a = new FreeVariable("a");
        BooleanVariable b = new FreeVariable("b");
        // f = a * b + b * a + a * b
        BooleanFormula f = or(or(and(a, b), and(b, a)), and(a, b));
        Cnf cnf = generator.generate(f);
        Set<Set<String>> expected = SetUtils.convertArraysToSets(new String[][]{{"a"}, {"b"}});
        Assert.assertEquals(expected, ClauseUtils.getLiteralSets(cnf));

    }

}
