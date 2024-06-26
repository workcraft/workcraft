package org.workcraft.formula;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.formula.bdd.BddManager;

class BddTests {

    private static final BddManager BDD_MANAGER = new BddManager();

    @Test
    void testUnateness() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");

        BooleanFormula and2Func = new And(aVar, bVar);
        checkUnateness(and2Func, aVar, true, false);

        BooleanFormula nor2Func = new Not(new Or(aVar, bVar));
        checkUnateness(nor2Func, aVar, false, true);

        BooleanFormula xor2Func = new Xor(aVar, bVar);
        checkUnateness(xor2Func, aVar, false, false);

        BooleanFormula mux2Func = new Or(new And(aVar, cVar), new And(bVar, new Not(cVar)));
        checkUnateness(mux2Func, aVar, true, false);
        checkUnateness(mux2Func, cVar, false, false);
    }

    private void checkUnateness(BooleanFormula formula, BooleanVariable var, boolean posUnate, boolean negUnate) {
        Assertions.assertEquals(posUnate, BDD_MANAGER.isPositiveUnate(formula, var));
        Assertions.assertEquals(negUnate, BDD_MANAGER.isNegativeUnate(formula, var));
        Assertions.assertEquals(!posUnate && !negUnate, BDD_MANAGER.isBinate(formula, var));
    }

    @Test
    void testEquality() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");
        BooleanVariable dVar = new FreeVariable("d");

        Assertions.assertTrue(BDD_MANAGER.isEquivalent(aVar,
                new Not(new Not(aVar))));

        Assertions.assertTrue(BDD_MANAGER.isEquivalent(new And(aVar, bVar),
                new Not(new Or(new Not(aVar), new Not(bVar)))));

        Assertions.assertTrue(BDD_MANAGER.isEquivalent(new And(aVar, new Not(bVar)),
                new Not(new Or(new Not(aVar), bVar))));

        Assertions.assertTrue(BDD_MANAGER.isEquivalent(new Or(new And(aVar, bVar), new And(cVar, dVar)),
                new Not(new And(new Or(new Not(dVar), new Not(cVar)), new Or(new Not(bVar), new Not(aVar))))));

        Assertions.assertTrue(BDD_MANAGER.isEquivalent(new Xor(aVar, bVar),
                new Or(new And(aVar, new Not(bVar)), new And(new Not(aVar), bVar))));

        Assertions.assertFalse(BDD_MANAGER.isEquivalent(new Xor(aVar, bVar),
                new And(new Or(aVar, new Not(bVar)), new Or(new Not(aVar), bVar))));
    }

}
