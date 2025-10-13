package org.workcraft.formula;

import org.workcraft.formula.visitors.*;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.formula.workers.DumbBooleanWorker;

import java.util.*;

public class FormulaUtils {

    public static BooleanFormula remove(BooleanFormula formula, BooleanVariable variable) {
        return replace(formula, variable, null, CleverBooleanWorker.getInstance());
    }

    public static BooleanFormula replaceOne(BooleanFormula formula, BooleanVariable variable) {
        return replace(formula, variable, One.getInstance(), CleverBooleanWorker.getInstance());
    }

    public static BooleanFormula replaceZero(BooleanFormula formula, BooleanVariable variable) {
        return replace(formula, variable, Zero.getInstance(), CleverBooleanWorker.getInstance());
    }

    public static BooleanFormula replace(BooleanFormula formula, BooleanVariable variable, BooleanFormula value) {
        return replace(formula, variable, value, DumbBooleanWorker.getInstance());
    }

    public static BooleanFormula replace(BooleanFormula formula, BooleanVariable variable,
            BooleanFormula value, BooleanWorker worker) {

        return replace(formula, Collections.singletonList(variable), Collections.singletonList(value), worker);
    }

    public static BooleanFormula replace(BooleanFormula formula, List<? extends BooleanVariable> variables,
            List<? extends BooleanFormula> values) {

        return replace(formula, variables, values, DumbBooleanWorker.getInstance());
    }

    public static BooleanFormula replace(BooleanFormula formula, List<? extends BooleanVariable> variables,
            List<? extends BooleanFormula> values, BooleanWorker worker) {

        if ((formula == null) || (variables == null) || (values == null) || variables.isEmpty() || values.isEmpty()) {
            return formula;
        }
        if (variables.size() != values.size()) {
            throw new RuntimeException("Length of the variable list must be equal to that of formula list.");
        }
        Map<BooleanVariable, BooleanFormula> variableToValueMap = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            variableToValueMap.put(variables.get(i), values.get(i));
        }
        return formula.accept(new BooleanReplacer(variableToValueMap, worker));
    }

    public static BooleanFormula replace(BooleanFormula formula,
            Map<? extends BooleanVariable, ? extends BooleanFormula> variableToValueMap) {

        return replace(formula, variableToValueMap, DumbBooleanWorker.getInstance());
    }

    public static BooleanFormula replace(BooleanFormula formula,
            Map<? extends BooleanVariable, ? extends BooleanFormula> variableToValueMap, BooleanWorker worker) {

        if ((formula == null) || (variableToValueMap == null) || variableToValueMap.isEmpty()) {
            return formula;
        }
        return formula.accept(new BooleanReplacer(variableToValueMap, worker));
    }

    public static BooleanFormula replaceBinateVariable(BooleanFormula formula,
            BooleanVariable variable, BooleanFormula value) {

        if ((formula == null) || (variable == null) || (value == null)) {
            return formula;
        }
        return formula.accept(new BinateVariableReplacer(variable, value));
    }

    public static Set<BooleanVariable> extractNegatedVariables(BooleanFormula formula) {
        Set<BooleanVariable> result = new HashSet<>();
        if (formula != null) {
            result.addAll(formula.accept(new NegatedVariableExtractor()));
        }
        return result;
    }

    public static int countLiterals(BooleanFormula formula) {
        int result = 0;
        if (formula != null) {
            result += formula.accept(new LiteralCounter());
        }
        return result;
    }

    public static Set<BooleanVariable> extractVariables(BooleanFormula formula) {
        Set<BooleanVariable> result = new HashSet<>();
        if (formula != null) {
            result.addAll(formula.accept(new VariableExtractor()));
        }
        return result;
    }

    public static List<BooleanVariable> extractOrderedVariables(BooleanFormula formula) {
        List<BooleanVariable> result = new ArrayList<>();
        if (formula != null) {
            result.addAll(new LinkedHashSet<>(formula.accept(new OrderedVariableExtractor())));
        }
        return result;
    }

    public static BooleanFormula invert(BooleanFormula formula) {
        return (formula == null) ? null : formula.accept(Inverter.getInstance());
    }

    public static BooleanFormula propagateInversion(BooleanFormula formula) {
        return (formula == null) ? null : FormulaUtils.invert(formula.accept(
                new BooleanComplementTransformer(CleverBooleanWorker.getInstance())));
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

    public static BooleanFormula createMux(BooleanFormula i0Var, BooleanFormula i1Var, BooleanFormula sVar) {
        return new Or(new And(i0Var, new Not(sVar)), new And(i1Var, sVar));
    }

    public static BooleanFormula createMaj(BooleanFormula a, BooleanFormula b, BooleanFormula c) {
        return new Or(new And(a, b), new Or(new And(a, c), new And(b, c)));
    }

}