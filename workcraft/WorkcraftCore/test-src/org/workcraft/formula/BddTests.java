package org.workcraft.formula;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.formula.bdd.BddManager;
import org.workcraft.formula.bdd.CuddBddManager;
import org.workcraft.formula.bdd.JddBddManager;
import org.workcraft.formula.visitors.StringGenerator;

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
        BooleanVariable c01Var = new FreeVariable("c01");
        BooleanVariable c11Var = new FreeVariable("c11");

        // func = m0' * m1 * c01 + m0 * m1 * c11
        BooleanFormula funcFormula = FormulaUtils.createSop(
                List.of(new Not(m0Var), m1Var, c01Var),
                List.of(m0Var, m1Var, c11Var));

        // proj(func, m0 * m1) = c11
        checkProjection(bddManager, funcFormula, new And(m0Var, m1Var), c11Var);

        // proj(func, m0) = c11 * m1
        checkProjection(bddManager, funcFormula, m0Var, new And(m1Var, c11Var));

        // proj(func, m1) = m0' * c01 + c11 * m0
        checkProjection(bddManager, funcFormula, m1Var, new Or(new And(new Not(m0Var), c01Var), new And(m0Var, c11Var)));

        // proj(func, m0') = m1 * c01
        checkProjection(bddManager, funcFormula, new Not(m0Var), new And(m1Var, c01Var));

        // proj(func, m1') = 0
        checkProjection(bddManager, funcFormula, new Not(m1Var), Zero.getInstance());

        // proj(func, m0' * m1) = c01
        checkProjection(bddManager, funcFormula, new And(new Not(m0Var), m1Var), c01Var);

        // proj(func, m0 * m1') = 0
        checkProjection(bddManager, funcFormula, new And(m0Var, new Not(m1Var)), Zero.getInstance());

        // proj(func, m0' * m1') = 0
        checkProjection(bddManager, funcFormula, new And(new Not(m0Var), new Not(m1Var)), Zero.getInstance());

        // proj(func, m0 * m1' + m0' * m1) = m1 * c01 [alternatively = m0' * c01]
        checkProjection(bddManager, funcFormula, new Or(new And(m0Var, new Not(m1Var)), new And(new Not(m0Var), m1Var)),
                new And(m1Var, c01Var));

        // proj(func, m0 ^ m1) = m1 * c01 [alternatively = m0' * c01]
        checkProjection(bddManager, funcFormula, new Xor(m0Var, m1Var), new And(m1Var, c01Var));

        checkStats(bddManager, 0, 4);
    }

    private void checkProjection(BddManager bddManager,
            BooleanFormula funcFormula, BooleanFormula modeFormula, BooleanFormula expProjectionFormula) {

        BooleanFormula projectionFormula = bddManager.calcProjectionFormula(funcFormula, modeFormula);
        System.out.println("projectionFormula = " + StringGenerator.toString(projectionFormula));
        Assertions.assertTrue(bddManager.isEquivalent(projectionFormula, expProjectionFormula));
    }

    @Test
    void testJddBuildIsopFormula() {
        try (BddManager bddManager = createJddBddManager()) {
            testBuildIsopFormula(bddManager);
        }
    }

    @Test
    void testCuddBuildIsopFormula() {
        try (BddManager bddManager = createCuddBddManager()) {
            testBuildIsopFormula(bddManager);
        }
    }

    void testBuildIsopFormula(BddManager bddManager) {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");
        BooleanVariable dVar = new FreeVariable("d");
        BooleanVariable eVar = new FreeVariable("e");
        BooleanVariable fVar = new FreeVariable("f");

        // func = a * b * c * d
        checkBuildIsopFormula(bddManager, FormulaUtils.createSop(List.of(aVar, bVar, cVar, dVar)));

        // func = a * b + c * d
        checkBuildIsopFormula(bddManager, FormulaUtils.createSop(List.of(aVar, bVar), List.of(cVar, dVar)));

        // func = a * b + b * c + c * a
        checkBuildIsopFormula(bddManager, FormulaUtils.createSop(List.of(aVar, bVar), List.of(bVar, cVar), List.of(cVar, aVar)));

        // func = a * b + a' * c + a' * d'
        checkBuildIsopFormula(bddManager, FormulaUtils.createSop(List.of(aVar, bVar), List.of(new Not(aVar), cVar), List.of(new Not(aVar), new Not(dVar))));

        // func = a * b' + a' * b
        checkBuildIsopFormula(bddManager, FormulaUtils.createSop(List.of(aVar, new Not(bVar)), List.of(new Not(aVar), bVar)));

        // func = a ^ b
        checkBuildIsopFormula(bddManager, new Xor(aVar, bVar));

        // func = a ^ b ^ c
        checkBuildIsopFormula(bddManager, new Xor(new Xor(aVar, bVar), cVar));

        // func = a ^ b ^ c ^ d
        checkBuildIsopFormula(bddManager, new Xor(new Xor(aVar, bVar), new Xor(cVar, dVar)));

        // func = (a + b) * (c + d) * (e + f)
        checkBuildIsopFormula(bddManager, FormulaUtils.createPos(List.of(aVar, bVar), List.of(cVar, dVar), List.of(eVar, fVar)));
    }

    private void checkBuildIsopFormula(BddManager bddManager, BooleanFormula funcFormula) {
        BooleanFormula projectionFormula = bddManager.calcProjectionFormula(funcFormula, One.getInstance());
        System.out.println(StringGenerator.toString(funcFormula) + " == " + StringGenerator.toString(projectionFormula));
        Assertions.assertTrue(bddManager.isEquivalent(funcFormula, projectionFormula));
    }

}
