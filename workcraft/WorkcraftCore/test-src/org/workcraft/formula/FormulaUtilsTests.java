package org.workcraft.formula;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.formula.workers.DumbBooleanWorker;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

class FormulaUtilsTests {

    @Test
    void testReplacement() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");
        BooleanVariable dVar = new FreeVariable("d");

        BooleanFormula andFormula = createAnd(aVar, bVar, cVar);
        BooleanFormula expectedAndFormula = createAnd(aVar, bVar, aVar);
        checkReplacement(andFormula, cVar, aVar, expectedAndFormula);

        BooleanFormula majFormula = FormulaUtils.createMaj(aVar, bVar, cVar);
        BooleanFormula expectedMajFormula = FormulaUtils.createMaj(aVar, bVar, dVar);
        checkReplacement(majFormula, cVar, dVar, expectedMajFormula);

        BooleanFormula muxFormula = FormulaUtils.createMux(aVar, bVar, cVar);
        BooleanFormula expectedMuxFormula = FormulaUtils.createMux(aVar, bVar, dVar);
        checkReplacement(muxFormula, cVar, dVar, expectedMuxFormula);
    }

    private BooleanFormula createAnd(BooleanVariable... variables) {
        return FormulaUtils.createAnd(Arrays.asList(variables), DumbBooleanWorker.getInstance());
    }

    private void checkReplacement(BooleanFormula formula, BooleanVariable variable, BooleanFormula replacement, BooleanFormula expectedFormula) {
        String formulaString = StringGenerator.toString(formula);
        String replacementString = StringGenerator.toString(replacement);
        BooleanFormula resultFormula = FormulaUtils.replace(formula, variable, replacement);
        String actualString = StringGenerator.toString(resultFormula);
        String expectedString = StringGenerator.toString(expectedFormula);
        System.out.println("[" + variable.getLabel() + "=" + replacementString + "]: " + formulaString + " -> " + actualString);
        Assertions.assertEquals(expectedString, actualString);
    }

    @Test
    void testReplaceBinateVariable() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable sVar = new FreeVariable("s");
        BooleanVariable posVar = new FreeVariable("pos");

        BooleanFormula muxFormula = new Or(new And(aVar, sVar), new And(bVar, new Not(sVar)));
        BooleanFormula expectedMuxFormula = new Or(new And(aVar, posVar), new And(bVar, new Not(sVar)));
        checkReplaceBinateVariable(muxFormula, sVar, posVar, expectedMuxFormula);
    }

    private void checkReplaceBinateVariable(BooleanFormula formula, BooleanVariable variable, BooleanVariable replacement, BooleanFormula expectedFormula) {
        String formulaString = StringGenerator.toString(formula);
        BooleanFormula resultFormula = FormulaUtils.replaceBinateVariable(formula, variable, replacement);
        String actualString = StringGenerator.toString(resultFormula);
        String expectedString = StringGenerator.toString(expectedFormula);
        System.out.println("[pos(" + variable.getLabel() + ")=" + replacement.getLabel() + "]: " + formulaString + " -> " + actualString);
        Assertions.assertEquals(expectedString, actualString);
    }

    @Test
    void testExtractVariables() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");

        BooleanFormula majFormula = FormulaUtils.createMaj(aVar, bVar, cVar);
        Assertions.assertEquals(6, FormulaUtils.countLiterals(majFormula));
        Assertions.assertEquals(Arrays.asList(aVar, bVar, cVar), FormulaUtils.extractOrderedVariables(majFormula));
        Assertions.assertEquals(Collections.emptySet(), FormulaUtils.extractNegatedVariables(majFormula));

        BooleanFormula muxFormula = FormulaUtils.createMux(aVar, bVar, cVar);
        Assertions.assertEquals(4, FormulaUtils.countLiterals(muxFormula));
        Assertions.assertEquals(Arrays.asList(aVar, cVar, bVar), FormulaUtils.extractOrderedVariables(muxFormula));
        Assertions.assertEquals(new HashSet(Arrays.asList(cVar)), FormulaUtils.extractNegatedVariables(muxFormula));
    }

    @Test
    void testInvert() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");

        BooleanFormula muxFormula = FormulaUtils.createMux(aVar, bVar, cVar);
        BooleanFormula expectedInvFormula = new Not(muxFormula);
        checkInvert(muxFormula, expectedInvFormula);

        checkInvert(Zero.getInstance(), One.getInstance());
    }

    private void checkInvert(BooleanFormula formula, BooleanFormula expectedFormula) {
        String formulaString = StringGenerator.toString(formula);
        BooleanFormula resultFormula = FormulaUtils.invert(formula);
        String actualString = StringGenerator.toString(resultFormula);
        String expectedString = StringGenerator.toString(expectedFormula);
        System.out.println("invert: " + formulaString + " -> " + actualString);
        Assertions.assertEquals(expectedString, actualString);
    }

    @Test
    void testDerive() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");
        BooleanVariable dVar = new FreeVariable("d");

        BooleanFormula muxFormula = new Or(new And(aVar, cVar), new And(bVar, new Not(dVar)));
        BooleanFormula expectedDerivativeFormula = new Xor(new And(bVar, new Not(dVar)), new Or(aVar, new And(bVar, new Not(dVar))));
        checkDerive(muxFormula, cVar, expectedDerivativeFormula);
    }

    private void checkDerive(BooleanFormula formula, BooleanVariable variable, BooleanFormula expectedFormula) {
        String formulaString = StringGenerator.toString(formula);
        BooleanFormula resultFormula = FormulaUtils.derive(formula, variable);
        String actualString = StringGenerator.toString(resultFormula);
        String expectedString = StringGenerator.toString(expectedFormula);
        System.out.println("derive [" + variable.getLabel() + "]: " + formulaString + " -> " + actualString);
        Assertions.assertEquals(expectedString, actualString);
    }

}
