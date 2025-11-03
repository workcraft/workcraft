package org.workcraft.plugins.circuit.naryformula;

import org.workcraft.formula.*;
import org.workcraft.types.Func2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SplitFormGenerator {

    private static final class DelegatingPrinter implements NaryBooleanFormulaVisitor<SplitForm> {

        private boolean negated = false;

        @Override
        public SplitForm visit(BooleanVariable variable) {
            return new SplitForm(variable);
        }

        @Override
        public SplitForm visitNot(NaryBooleanFormula arg) {
            negated = !negated;
            return arg.accept(this);
        }

        @Override
        public SplitForm visitAnd(List<NaryBooleanFormula> args) {
            return visitNary(args, And::new);
        }

        @Override
        public SplitForm visitOr(List<NaryBooleanFormula> args) {
            return visitNary(args, Or::new);
        }

        @Override
        public SplitForm visitXor(List<NaryBooleanFormula> args) {
            return visitNary(args, Xor::new);
        }

        private SplitForm visitNary(List<NaryBooleanFormula> args,
                Func2<BooleanFormula, BooleanFormula, BooleanFormula> operation) {

            // Build the n-input formula
            Map<NaryBooleanFormula, BooleanVariable> argToVarMap = new HashMap<>();
            BooleanFormula formula = null;

            // Note that O (15th letter of the alphabet) is reserved for the name of output pin, therefore:
            //  - if the number of inputs is less than 15, then name the pins as A, B, C,... M, N
            //  - otherwise name input pins in the form i0, i1, i2,...
            boolean fewerThan15Inputs = args.size() < ('O' - 'A');
            int index = 0;
            for (NaryBooleanFormula arg: args) {
                String label = fewerThan15Inputs ? String.valueOf((char) ('A' + index)) : ("i" + index);
                index++;
                BooleanVariable variable = new FreeVariable(label);
                argToVarMap.put(arg, variable);
                BooleanFormula oldFormula  = formula;
                formula = variable;
                if (oldFormula != null) {
                    formula = operation.eval(oldFormula, formula);
                }
            }
            if (negated) {
                formula = new Not(formula);
                negated = false;
            }
            // Process the formula operands
            List<SplitForm> operands = new LinkedList<>();
            for (NaryBooleanFormula arg: args) {
                SplitForm operand = arg.accept(this);
                if (negated) {
                    BooleanVariable variable = argToVarMap.get(arg);
                    BooleanFormula value = FormulaUtils.invert(new FreeVariable(variable.getLabel() + "N"));
                    formula = FormulaUtils.replace(formula, variable, value);
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
            formula = One.getInstance();
        }
        NaryBooleanFormula naryFormula = NaryBooleanFormulaBuilder.build(formula);
        return naryFormula.accept(new DelegatingPrinter());
    }

}
