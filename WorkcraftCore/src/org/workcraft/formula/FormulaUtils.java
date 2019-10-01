package org.workcraft.formula;

import org.workcraft.formula.visitors.*;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.formula.workers.DumbBooleanWorker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FormulaUtils {

    public static BooleanFormula replaceOne(BooleanFormula formula, BooleanVariable variable) {
        return replace(formula, variable, One.getInstance(), CleverBooleanWorker.getInstance());
    }

    public static BooleanFormula replaceZero(BooleanFormula formula, BooleanVariable variable) {
        return replace(formula, variable, Zero.getInstance(), CleverBooleanWorker.getInstance());
    }

    public static BooleanFormula replace(BooleanFormula formula, BooleanVariable param, BooleanFormula value) {
        return replace(formula, param, value, DumbBooleanWorker.getInstance());
    }

    public static BooleanFormula replace(BooleanFormula formula, BooleanVariable variable,
            BooleanFormula value, BooleanWorker worker) {

        return replace(formula, Arrays.asList(variable), Arrays.asList(value), worker);
    }

    public static BooleanFormula replace(BooleanFormula formula, List<? extends BooleanVariable> variables,
            List<? extends BooleanFormula> values) {

        return replace(formula, variables, values, DumbBooleanWorker.getInstance());
    }

    public static BooleanFormula replace(BooleanFormula formula, List<? extends BooleanVariable> variables,
            List<? extends BooleanFormula> values, BooleanWorker worker) {

        BooleanFormula result = null;
        if (formula != null) {
            result = formula.accept(new BooleanReplacer(variables, values, worker));
        }
        return result;
    }

    public static BooleanFormula replaceBinateVariable(BooleanFormula formula, BooleanVariable variable, BooleanFormula value) {
        BooleanFormula result = null;
        if (formula != null) {
            result = formula.accept(new BinateVariableReplacer(variable, value));
        }
        return result;
    }

    public static Set<BooleanVariable> extractNegatedVariables(BooleanFormula formula) {
        Set<BooleanVariable> result = null;
        if (formula != null) {
            result = formula.accept(new NegatedVariableExtractor());
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

    public static List<BooleanVariable> extractOrderedVariables(BooleanFormula formula) {
        List<BooleanVariable> result = null;
        if (formula != null) {
            result = new ArrayList<>(formula.accept(new OrderedVariableExtractor()));
        }
        return result;
    }

    public static BooleanFormula invert(BooleanFormula formula) {
        return formula.accept(Inverter.getInstance());
    }

    public static BooleanFormula derive(BooleanFormula formula, BooleanVariable variable) {
        BooleanFormula zeroSubFormula = FormulaUtils.replaceZero(formula, variable);
        BooleanFormula oneSubFormula = FormulaUtils.replaceOne(formula, variable);
        return new Xor(zeroSubFormula, oneSubFormula);
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
