package org.workcraft.formula;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.formula.bdd.BddManager;
import org.workcraft.formula.bdd.CuddBddManager;
import org.workcraft.formula.bdd.JddBddManager;

import java.util.List;
import java.util.Map;

class BddTests {

    private static final boolean PRINT_CLOSE_STATS = false;

    private static BddManager createJddBddManager() {
        return new JddBddManager(PRINT_CLOSE_STATS);
    }

    private static BddManager createCuddBddManager() {
        return new CuddBddManager(PRINT_CLOSE_STATS);
    }

    private void checkStats(BddManager bddManager, int expRefBddCount, int expVarCount) {
        Map<String, Integer> stats = bddManager.getStats();
        Assertions.assertEquals(expRefBddCount, stats.get(BddManager.REFERENCED_BDD_COUNT_KEY));
        Assertions.assertEquals(expVarCount, stats.get(BddManager.VARIABLE_COUNT_KEY));
    }

    @Test
    void testJddUnateness() {
        try (BddManager bddManager = createJddBddManager()) {
            testUnateness(bddManager);
        }
    }

    @Test
    void testCuddUnateness() {
        try (BddManager bddManager = createCuddBddManager()) {
            testUnateness(bddManager);
        }
    }

    void testUnateness(BddManager bddManager) {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");

        BooleanFormula and2Func = new And(aVar, bVar);
        checkUnateness(bddManager, and2Func, aVar, true, false);

        BooleanFormula nor2Func = new Not(new Or(aVar, bVar));
        checkUnateness(bddManager, nor2Func, aVar, false, true);

        BooleanFormula xor2Func = new Xor(aVar, bVar);
        checkUnateness(bddManager, xor2Func, aVar, false, false);

        BooleanFormula mux2Func = new Or(new And(aVar, cVar), new And(bVar, new Not(cVar)));
        checkUnateness(bddManager, mux2Func, aVar, true, false);
        checkUnateness(bddManager, mux2Func, cVar, false, false);

        checkStats(bddManager, 0, 3);
    }

    private void checkUnateness(BddManager bddManager,
            BooleanFormula formula, BooleanVariable var, boolean posUnate, boolean negUnate) {

        Assertions.assertEquals(posUnate, bddManager.isPositiveUnate(formula, var));
        Assertions.assertEquals(negUnate, bddManager.isNegativeUnate(formula, var));
        Assertions.assertEquals(!posUnate && !negUnate, bddManager.isBinate(formula, var));
    }

    @Test
    void testJddEquality() {
        try (BddManager bddManager = createJddBddManager()) {
            testEquality(bddManager);
        }
    }

    @Test
    void testCuddEquality() {
        try (BddManager bddManager = createCuddBddManager()) {
            testEquality(bddManager);
        }
    }

    void testEquality(BddManager bddManager) {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");
        BooleanVariable dVar = new FreeVariable("d");

        checkEquality(bddManager, new Not(new And(new Or(new Not(dVar), new Not(cVar)), new Or(new Not(bVar), new Not(aVar)))),
                new Or(new And(aVar, bVar), new And(cVar, dVar)), true);

        checkEquality(bddManager, aVar, new Not(new Not(aVar)), true);

        checkEquality(bddManager, new And(aVar, bVar), new Not(new Or(new Not(aVar), new Not(bVar))), true);

        checkEquality(bddManager, new And(aVar, new Not(bVar)), new Not(new Or(new Not(aVar), bVar)), true);

        checkEquality(bddManager, new Not(new And(new Or(new Not(dVar), new Not(cVar)), new Or(new Not(bVar), new Not(aVar)))),
                new Or(new And(aVar, bVar), new And(cVar, dVar)), true);

        checkEquality(bddManager, new Or(new And(aVar, new Not(bVar)), new And(new Not(aVar), bVar)),
                new Xor(aVar, bVar), true);

        checkEquality(bddManager, new And(new Or(aVar, new Not(bVar)), new Or(new Not(aVar), bVar)),
                new Xor(aVar, bVar), false);

        checkStats(bddManager, 0, 4);
    }

    private static void checkEquality(BddManager bddManager,
            BooleanFormula leftFormula, BooleanFormula rightFormula, boolean expResult) {

        Assertions.assertEquals(expResult, bddManager.isEquivalent(leftFormula, rightFormula));
    }

    @Test
    void testJddProjection() {
        try (BddManager bddManager = createJddBddManager()) {
            testProjection(bddManager);
        }
    }

    @Test
    void testCuddProjection() {
        try (BddManager bddManager = createCuddBddManager()) {
            testProjection(bddManager);
        }
    }

    void testProjection(BddManager bddManager) {
        BooleanVariable m0Var = new FreeVariable("m0");
        BooleanVariable m1Var = new FreeVariable("m1");
        BooleanVariable cond01Var = new FreeVariable("cond01");
        BooleanVariable cond11Var = new FreeVariable("cond11");

        // func = m0' * m1 * cond01 + m0 * m1 * cond11
        BooleanFormula funcFormula = FormulaUtils.createSop(
                List.of(new Not(m0Var), m1Var, cond01Var),
                List.of(m0Var, m1Var, cond11Var));

        // proj(func, m0 * m1) = cond11
        checkProjection(bddManager, funcFormula, new And(m0Var, m1Var), cond11Var);

        // proj(func, m0) = cond11 * m1
        checkProjection(bddManager, funcFormula, m0Var, new And(m1Var, cond11Var));

        // proj(func, m1) = m0' * cond01 + cond11 * m0
        checkProjection(bddManager, funcFormula, m1Var, new Or(new And(new Not(m0Var), cond01Var), new And(m0Var, cond11Var)));

        // Note: Jdd only supports projection on a set of variables, so cannot be used for the following care-sets
        if (bddManager instanceof CuddBddManager) {
            // proj(func, m0' * m1') = 0
            checkProjection(bddManager, funcFormula, new And(new Not(m0Var), new Not(m1Var)), Zero.getInstance());

            // proj(func, m0 * m1' + m0' * m1) = m0' * cond01
            checkProjection(bddManager, funcFormula, new Or(new And(m0Var, new Not(m1Var)), new And(new Not(m0Var), m1Var)),
                    new And(new Not(m0Var), cond01Var));

            // proj(func, m0 ^ m1) = m0' * cond01
            checkProjection(bddManager, funcFormula, new Xor(m0Var, m1Var), new And(new Not(m0Var), cond01Var));
        }
        checkStats(bddManager, 0, 4);
    }

    private void checkProjection(BddManager bddManager,
            BooleanFormula funcFormula, BooleanFormula modeFormula, BooleanFormula expProjectionFormula) {

        BooleanFormula projectionFormula = bddManager.calcProjectionFormula(funcFormula, modeFormula);
        Assertions.assertTrue(bddManager.isEquivalent(projectionFormula, expProjectionFormula));
    }

}
