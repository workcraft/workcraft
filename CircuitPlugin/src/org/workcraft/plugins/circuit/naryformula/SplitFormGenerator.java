package org.workcraft.plugins.circuit.naryformula;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.workcraft.formula.And;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FreeVariable;
import org.workcraft.formula.Not;
import org.workcraft.formula.One;
import org.workcraft.formula.Or;
import org.workcraft.formula.Xor;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.util.Func2;

public class SplitFormGenerator {

    private static class DelegatingPrinter implements NaryBooleanFormulaVisitor<SplitForm> {

        private boolean negated = false;

        @Override
        public SplitForm visit(BooleanVariable variable) {
            return new SplitForm();
        }

        @Override
        public SplitForm visitNot(NaryBooleanFormula arg) {
            negated = !negated;
            return arg.accept(this);
        }

        @Override
        public SplitForm visitAnd(List<NaryBooleanFormula> args) {
            return visitNary(args, (arg0, arg1) -> new And(arg0, arg1));
        }

        @Override
        public SplitForm visitOr(List<NaryBooleanFormula> args) {
            return visitNary(args, (arg0, arg1) -> new Or(arg0, arg1));
        }

        @Override
        public SplitForm visitXor(List<NaryBooleanFormula> args) {
            return visitNary(args, (arg0, arg1) -> new Xor(arg0, arg1));
        }

        private SplitForm visitNary(List<NaryBooleanFormula> args,
                Func2<BooleanFormula, BooleanFormula, BooleanFormula> operation) {
            // Build the n-input formula
            Map<NaryBooleanFormula, BooleanVariable> argToVarMap = new HashMap<>();
            BooleanFormula formula = null;
            int index = 0;
            for (NaryBooleanFormula arg: args) {
                BooleanVariable variable = new FreeVariable("i" + index++);
                argToVarMap.put(arg, variable);
                BooleanFormula oldFormula  = formula;
                formula = variable;
                if (oldFormula  != null) {
                    formula = operation.eval(oldFormula, formula);
                }
            }
            if (negated) {
                formula = new Not(formula);
                negated = !negated;
            }
            // Process the formula operands
            List<SplitForm> operands = new LinkedList<>();
            for (NaryBooleanFormula arg: args) {
                SplitForm operand = arg.accept(this);
                if (negated) {
                    BooleanVariable variable = argToVarMap.get(arg);
                    formula = BooleanUtils.dumbReplace(formula, variable, new Not(variable));
                    negated = !negated;
                }
                operands.add(operand);
            }
            // Combine the formula and ins operands together
            SplitForm result = new SplitForm();
            result.addClauses(formula);
            result.addAll(operands);
            return result;
        }

    }

    public static SplitForm generate(BooleanFormula formula) {
        if (formula == null) {
            formula = One.instance();
        }
        NaryBooleanFormula naryFormula = NaryBooleanFormulaBuilder.build(formula);
        return naryFormula.accept(new DelegatingPrinter());
    }

}
