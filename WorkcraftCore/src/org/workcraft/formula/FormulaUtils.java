package org.workcraft.formula;

import org.workcraft.formula.visitors.BooleanReplacer;
import org.workcraft.formula.visitors.LiteralCounter;
import org.workcraft.formula.workers.BooleanWorker;

import java.util.Arrays;
import java.util.List;

public class FormulaUtils {

    public static BooleanFormula replace(BooleanFormula formula, List<? extends BooleanVariable> params,
            List<? extends BooleanFormula> values, BooleanWorker worker) {

        BooleanFormula result = null;
        if (formula != null) {
            result = formula.accept(new BooleanReplacer(params, values, worker));
        }
        return result;
    }

    public static BooleanFormula replace(BooleanFormula formula, BooleanVariable param,
            BooleanFormula value, BooleanWorker worker) {

        return replace(formula, Arrays.asList(param), Arrays.asList(value), worker);
    }

    public static BooleanFormula replaceOne(BooleanFormula formula, BooleanVariable param, BooleanWorker worker) {
        return replace(formula, param, One.getInstance(), worker);
    }

    public static BooleanFormula replaceZero(BooleanFormula formula, BooleanVariable param, BooleanWorker worker) {
        return replace(formula, param, Zero.getInstance(), worker);
    }

    public static Integer countLiterals(BooleanFormula formula) {
        Integer result = null;
        if (formula != null) {
            result = formula.accept(new LiteralCounter());
        }
        return result;
    }

    public static BooleanFormula createAnd(List<? extends BooleanFormula> operands, BooleanWorker worker) {
        return createAnd(operands, 0, operands.size(), worker);
    }

    private static BooleanFormula createAnd(List<? extends BooleanFormula> operands, int start, int end, BooleanWorker worker) {
        int size = end - start;
        if (size == 0) {
            return One.getInstance();
        } else {
            if (size == 1) {
                return operands.get(start);
            } else {
                int split = (end + start) / 2;
                return worker.and(createAnd(operands, start, split, worker), createAnd(operands, split, end, worker));
            }
        }
    }

    public static BooleanFormula createOr(List<? extends BooleanFormula> operands, BooleanWorker worker) {
        return createOr(operands, 0, operands.size(), worker);
    }

    private static BooleanFormula createOr(List<? extends BooleanFormula> operands, int start, int end, BooleanWorker worker) {
        int size = end - start;
        if (size == 0) {
            return Zero.getInstance();
        } else {
            if (size == 1) {
                return operands.get(start);
            } else {
                int split = (end + start) / 2;
                return worker.or(createOr(operands, start, split, worker), createOr(operands, split, end, worker));
            }
        }
    }

}
