package org.workcraft.formula.utils;

import java.util.Arrays;
import java.util.List;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.BooleanWorker;
import org.workcraft.formula.CleverBooleanWorker;
import org.workcraft.formula.DumbBooleanWorker;
import org.workcraft.formula.MemoryConservingBooleanWorker;
import org.workcraft.formula.PrettifyBooleanWorker;

public class BooleanUtils {

    public static BooleanFormula replaceClever(BooleanFormula formula,
            List<? extends BooleanVariable> params, List<? extends BooleanFormula> values) {
        BooleanFormula result = null;
        if (formula != null) {
            CleverBooleanWorker worker = new CleverBooleanWorker();
            result = formula.accept(new BooleanReplacer(params, values, worker));
        }
        return result;
    }

    public static BooleanFormula replaceClever(BooleanFormula formula, BooleanVariable param, BooleanFormula value) {
        return replaceClever(formula, Arrays.asList(param), Arrays.asList(value));
    }

    public static BooleanFormula replaceDumb(BooleanFormula formula,
            List<? extends BooleanVariable> params, List<? extends BooleanFormula> values) {
        DumbBooleanWorker worker = new DumbBooleanWorker();
        return formula.accept(new BooleanReplacer(params, values, worker));
    }

    public static BooleanFormula replaceDumb(BooleanFormula formula, BooleanVariable param, BooleanFormula value) {
        BooleanFormula result = null;
        if (formula != null) {
            result = replaceDumb(formula, Arrays.asList(param), Arrays.asList(value));
        }
        return result;
    }

    public static BooleanFormula replacePretty(BooleanFormula formula,
            List<? extends BooleanVariable> params, List<? extends BooleanFormula> values) {
        BooleanWorker worker = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());
        return formula.accept(new BooleanReplacer(params, values, worker));
    }

    public static BooleanFormula replacePretty(BooleanFormula formula, BooleanVariable param, BooleanFormula value) {
        BooleanFormula result = null;
        if (formula != null) {
            result = replacePretty(formula, Arrays.asList(param), Arrays.asList(value));
        }
        return result;
    }

    public static Integer countLiterals(BooleanFormula formula) {
        Integer result = null;
        if (formula != null) {
            result = formula.accept(new LiteralCounter());
        }
        return result;
    }

    public static BooleanFormula transformDeMorgan(BooleanFormula formula) {
        BooleanFormula result = null;
        if (formula != null) {
            result = BooleanOperations.not(formula.accept(new BooleanComplementTransformer()));
        }
        return result;
    }

    public static boolean compareFunctions(BooleanFormula func1, BooleanFormula func2) {
        String setFunctionString = func1.accept(new StringGenerator());
        String demorganFunctionString = func2.accept(new StringGenerator());
        return setFunctionString.equals(demorganFunctionString);
    }

}
