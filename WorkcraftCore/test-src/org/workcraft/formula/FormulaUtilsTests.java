package org.workcraft.formula;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.formula.workers.DumbBooleanWorker;

import java.util.Arrays;
import java.util.HashSet;

public class FormulaUtilsTests {

    @Test
    public void testReplacement() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable cVar = new FreeVariable("c");
        BooleanVariable dVar = new FreeVariable("d");

        BooleanFormula andFormula = buildAndFormula(aVar, bVar, cVar);
        BooleanFormula expectedAndFormula = buildAndFormula(aVar, bVar, aVar);
        checkReplacement(andFormula, cVar, aVar, expectedAndFormula);

        BooleanFormula muxFormula = buildMuxFormula(aVar, bVar, cVar);
        BooleanFormula expectedMuxFormula = buildMuxFormula(aVar, bVar, dVar);
        checkReplacement(muxFormula, cVar, dVar, expectedMuxFormula);
    }

    private BooleanFormula buildAndFormula(BooleanVariable... variables) {
        return FormulaUtils.createAnd(Arrays.asList(variables), DumbBooleanWorker.getInstance());
    }

    private BooleanFormula buildMuxFormula(BooleanVariable aVar, BooleanVariable bVar, BooleanVariable sVar) {
        return new Or(new And(aVar, sVar), new And(bVar, new Not(sVar)));
    }


    private void checkReplacement(BooleanFormula formula, BooleanVariable variable, BooleanFormula replacement, BooleanFormula expectedFormula) {
        String formulaString = StringGenerator.toString(formula);
        String replacementString = StringGenerator.toString(replacement);
        BooleanFormula resultFormula = FormulaUtils.replace(formula, variable, replacement);
        String actualString = StringGenerator.toString(resultFormula);
        String expectedString = StringGenerator.toString(expectedFormula);
        System.out.println("[" + variable.getLabel() + "=" + replacementString + "]: " + formulaString + " -> " + actualString);
        Assert.assertEquals(expectedString, actualString);
    }

    @Test
    public void testReplaceBinateVariable() {
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
        Assert.assertEquals(expectedString, actualString);
    }

    @Test
    public void testExtractVariables() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable sVar = new FreeVariable("s");

        BooleanFormula muxFormula = buildMuxFormula(aVar, bVar, sVar);
        Assert.assertEquals(4, FormulaUtils.countLiterals(muxFormula));
        Assert.assertEquals(Arrays.asList(aVar, sVar, bVar), FormulaUtils.extractOrderedVariables(muxFormula));
        Assert.assertEquals(new HashSet(Arrays.asList(sVar)), FormulaUtils.extractNegatedVariables(muxFormula));
    }

    @Test
    public void testInvert() {
        BooleanVariable aVar = new FreeVariable("a");
        BooleanVariable bVar = new FreeVariable("b");
        BooleanVariable sVar = new FreeVariable("s");

        BooleanFormula muxFormula = buildMuxFormula(aVar, bVar, sVar);
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
        Assert.assertEquals(expectedString, actualString);
    }

    @Test
    public void testDerive() {
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
        Assert.assertEquals(expectedString, actualString);
    }

}
