package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

public class BinateVariableReplacer implements BooleanVisitor<BooleanFormula> {

    private final BooleanVariable variable;
    private final BooleanFormula replacement;
    private boolean negation = false;

    public BinateVariableReplacer(BooleanVariable variable, BooleanFormula replacement) {
        this.variable = variable;
        this.replacement = replacement;
    }

    @Override
    public BooleanFormula visit(Zero node) {
        return node;
    }

    @Override
    public BooleanFormula visit(One node) {
        return node;
    }

    @Override
    public BooleanFormula visit(BooleanVariable node) {
        if ((node == variable) && !negation) {
            negation = false;
            return replacement;
        }
        return node;
    }

    @Override
    public BooleanFormula visit(Not node) {
        negation = true;
        return new Not(node.getX().accept(this));
    }

    @Override
    public BooleanFormula visit(And node) {
        return visitBinaryOperator(node, And::new);
    }

    @Override
    public BooleanFormula visit(Or node) {
        return visitBinaryOperator(node, Or::new);
    }

    @Override
    public BooleanFormula visit(Xor node) {
        return visitBinaryOperator(node, Xor::new);
    }

    @Override
    public BooleanFormula visit(Imply node) {
        return visitBinaryOperator(node, Imply::new);
    }

    @Override
    public BooleanFormula visit(Iff node) {
        return visitBinaryOperator(node, Iff::new);
    }

    private BooleanFormula visitBinaryOperator(BinaryBooleanFormula node, BooleanReplacer.BinaryOperation op) {
        negation = false;
        return op.apply(node.getX().accept(this), node.getY().accept(this));
    }

}
