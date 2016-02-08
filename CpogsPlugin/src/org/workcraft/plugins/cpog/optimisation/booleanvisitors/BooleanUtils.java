package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.CleverBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.MemoryConservingBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.PrettifyBooleanWorker;

public class BooleanUtils {

    public static BooleanFormula cleverReplace(BooleanFormula formula,
            List<? extends BooleanVariable> params,    List<? extends BooleanFormula> values) {
        CleverBooleanWorker worker = new CleverBooleanWorker();
        return formula.accept(new BooleanReplacer(params, values, worker));
    }

    public static BooleanFormula cleverReplace(BooleanFormula formula, BooleanVariable param, BooleanFormula value) {
        BooleanFormula result = null;
        if (formula != null) {
            result = cleverReplace(formula, Arrays.asList(param), Arrays.asList(value));
        }
        return result;
    }

    public static BooleanFormula cleverReplace(BooleanFormula formula) {
        BooleanFormula result = null;
        if (formula != null) {
            result = cleverReplace(formula, new ArrayList<BooleanVariable>(),  new ArrayList<BooleanFormula>());
        }
        return result;
    }


    public static BooleanFormula dumbReplace(BooleanFormula formula,
            List<? extends BooleanVariable> params,    List<? extends BooleanFormula> values) {
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
            result = dumbReplace(formula, new ArrayList<BooleanVariable>(),  new ArrayList<BooleanFormula>());
        }
        return result;
    }


    public static BooleanFormula prettifyReplace(BooleanFormula formula,
            List<? extends BooleanVariable> params,    List<? extends BooleanFormula> values) {
        BooleanWorker worker = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());
        return formula.accept(new BooleanReplacer(params, values, worker));
    }

    public static BooleanFormula prettifyReplace(BooleanFormula formula, BooleanVariable param,    BooleanFormula value) {
        BooleanFormula result = null;
        if (formula != null) {
            result = prettifyReplace(formula, Arrays.asList(param), Arrays.asList(value));
        }
        return result;
    }

    public static BooleanFormula prettifyReplace(BooleanFormula formula) {
        BooleanFormula result = null;
        if (formula != null) {
            result = prettifyReplace(formula, new ArrayList<BooleanVariable>(),  new ArrayList<BooleanFormula>());
        }
        return result;
    }

}
