package org.workcraft.formula.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.BooleanWorker;
import org.workcraft.formula.CleverBooleanWorker;
import org.workcraft.formula.DumbBooleanWorker;
import org.workcraft.formula.MemoryConservingBooleanWorker;
import org.workcraft.formula.PrettifyBooleanWorker;

public class BooleanUtils {

    public static BooleanFormula cleverReplace(BooleanFormula formula,
            List<? extends BooleanVariable> params, List<? extends BooleanFormula> values) {
        BooleanFormula result = null;
        if (formula != null) {
            CleverBooleanWorker worker = new CleverBooleanWorker();
            result = formula.accept(new BooleanReplacer(params, values, worker));
        }
        return result;
    }

    public static BooleanFormula cleverReplace(BooleanFormula formula, BooleanVariable param, BooleanFormula value) {
        return cleverReplace(formula, Arrays.asList(param), Arrays.asList(value));
    }

    public static BooleanFormula cleverReplace(BooleanFormula formula) {
        BooleanFormula result = null;
        if (formula != null) {
            result = cleverReplace(formula, new ArrayList<BooleanVariable>(), new ArrayList<BooleanFormula>());
        }
        return result;
    }

    public static BooleanFormula dumbReplace(BooleanFormula formula,
            List<? extends BooleanVariable> params, List<? extends BooleanFormula> values) {
        DumbBooleanWorker worker = new DumbBooleanWorker();
        return formula.accept(new BooleanReplacer(params, values, worker));
    }

    public static BooleanFormula dumbReplace(BooleanFormula formula, BooleanVariable param, BooleanFormula value) {
        BooleanFormula result = null;
        if (formula != null) {
            result = dumbReplace(formula, Arrays.asList(param), Arrays.asList(value));
        }
        return result;
    }

    public static BooleanFormula dumbReplace(BooleanFormula formula) {
        BooleanFormula result = null;
        if (formula != null) {
            result = dumbReplace(formula, new ArrayList<BooleanVariable>(), new ArrayList<BooleanFormula>());
        }
        return result;
    }

    public static BooleanFormula prettifyReplace(BooleanFormula formula,
            List<? extends BooleanVariable> params, List<? extends BooleanFormula> values) {
        BooleanWorker worker = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());
        return formula.accept(new BooleanReplacer(params, values, worker));
    }

    public static BooleanFormula prettifyReplace(BooleanFormula formula, BooleanVariable param, BooleanFormula value) {
        BooleanFormula result = null;
        if (formula != null) {
            result = prettifyReplace(formula, Arrays.asList(param), Arrays.asList(value));
        }
        return result;
    }

    public static BooleanFormula prettifyReplace(BooleanFormula formula) {
        BooleanFormula result = null;
        if (formula != null) {
            result = prettifyReplace(formula, new ArrayList<BooleanVariable>(), new ArrayList<BooleanFormula>());
        }
        return result;
    }

}
