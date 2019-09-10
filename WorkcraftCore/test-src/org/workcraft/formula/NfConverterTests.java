package org.workcraft.formula;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.formula.cnf.CnfGenerator;
import org.workcraft.formula.dnf.DnfGenerator;
import org.workcraft.formula.utils.ClauseUtils;
import org.workcraft.utils.SetUtils;

import static org.workcraft.formula.BooleanOperations.*;

public class NfConverterTests {

    private static BooleanVariable a = new FreeVariable("a");
    private static BooleanVariable b = new FreeVariable("b");
    private static BooleanVariable c = new FreeVariable("c");
    // a * b + b * a + a * b
    private static BooleanFormula f1 = or(or(and(a, b), and(b, a)), and(a, b));
    // a * (b + c)
    private static BooleanFormula f2 = and(a, or(b, c));
    // a + b * c
    private static BooleanFormula f3 = or(a, and(b, c));
    // a * b + b' * c
    private static BooleanFormula f4 = or(and(a, b), and(not(b), c));

    @Test
    public void testDnfConverter() {
        testDnfConverter(new String[][]{{"a", "b"}}, f1);
        testDnfConverter(new String[][]{{"a", "b"}, {"a", "c"}}, f2);
        testDnfConverter(new String[][]{{"a"}, {"b", "c"}}, f3);
        testDnfConverter(new String[][]{{"a", "b"}, {"b'", "c"}}, f4);
    }

    private void testDnfConverter(String[][] s, BooleanFormula f) {
        Assert.assertEquals(SetUtils.convertArraysToSets(s), ClauseUtils.getLiteralSets(DnfGenerator.generate(f)));
    }

    @Test
    public void testCnfConverter() {
        testCnfConverter(new String[][]{{"a"}, {"b"}}, f1);
        testCnfConverter(new String[][]{{"a"}, {"b", "c"}}, f2);
        testCnfConverter(new String[][]{{"a", "b"}, {"a", "c"}}, f3);
        testCnfConverter(new String[][]{{"a", "b'"}, {"a", "c"}, {"b", "c"}}, f4);
    }

    private void testCnfConverter(String[][] s, BooleanFormula f) {
        Assert.assertEquals(SetUtils.convertArraysToSets(s), ClauseUtils.getLiteralSets(CnfGenerator.generate(f)));
    }

}
