package org.workcraft.formula;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.formula.cnf.CnfGenerator;
import org.workcraft.formula.dnf.DnfGenerator;
import org.workcraft.formula.workers.DumbBooleanWorker;
import org.workcraft.utils.SetUtils;

class NfConverterTests {

    private static final DumbBooleanWorker WORKER = DumbBooleanWorker.getInstance();

    private static BooleanVariable a = new FreeVariable("a");
    private static BooleanVariable b = new FreeVariable("b");
    private static BooleanVariable c = new FreeVariable("c");
    // a * b + b * a + a * b
    private static BooleanFormula f1 = WORKER.or(WORKER.or(WORKER.and(a, b), WORKER.and(b, a)), WORKER.and(a, b));
    // a * (b + c)
    private static BooleanFormula f2 = WORKER.and(a, WORKER.or(b, c));
    // a + b * c
    private static BooleanFormula f3 = WORKER.or(a, WORKER.and(b, c));
    // a * b + b' * c
    private static BooleanFormula f4 = WORKER.or(WORKER.and(a, b), WORKER.and(WORKER.not(b), c));

    @Test
    void testDnfConverter() {
        testDnfConverter(new String[][]{{"a", "b"}}, f1);
        testDnfConverter(new String[][]{{"a", "b"}, {"a", "c"}}, f2);
        testDnfConverter(new String[][]{{"a"}, {"b", "c"}}, f3);
        testDnfConverter(new String[][]{{"a", "b"}, {"b'", "c"}}, f4);
    }

    private void testDnfConverter(String[][] s, BooleanFormula f) {
        Assertions.assertEquals(SetUtils.convertArraysToSets(s), ClauseUtils.getLiteralSets(DnfGenerator.generate(f)));
    }

    @Test
    void testCnfConverter() {
        testCnfConverter(new String[][]{{"a"}, {"b"}}, f1);
        testCnfConverter(new String[][]{{"a"}, {"b", "c"}}, f2);
        testCnfConverter(new String[][]{{"a", "b"}, {"a", "c"}}, f3);
        testCnfConverter(new String[][]{{"a", "b'"}, {"a", "c"}, {"b", "c"}}, f4);
    }

    private void testCnfConverter(String[][] s, BooleanFormula f) {
        Assertions.assertEquals(SetUtils.convertArraysToSets(s), ClauseUtils.getLiteralSets(CnfGenerator.generate(f)));
    }

}
