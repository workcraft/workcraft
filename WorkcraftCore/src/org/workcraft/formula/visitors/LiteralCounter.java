package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

public class LiteralCounter implements BooleanVisitor<Integer> {

    @Override
    public Integer visit(Zero node) {
        return 1;
    }

    @Override
    public Integer visit(One node) {
        return 1;
    }

    @Override
    public Integer visit(BooleanVariable node) {
        return 1;
    }

    @Override
    public Integer visit(Not node) {
        return node.getX().accept(this);
    }

    @Override
    public Integer visit(And node) {
        return visitBinaryOperator(node);
    }

    @Override
    public Integer visit(Or node) {
        return visitBinaryOperator(node);
    }

    @Override
    public Integer visit(Iff node) {
        return visitBinaryOperator(node);
    }

    @Override
    public Integer visit(Xor node) {
        return visitBinaryOperator(node);
    }

    @Override
    public Integer visit(Imply node) {
        return visitBinaryOperator(node);
    }

    private Integer visitBinaryOperator(BinaryBooleanFormula node) {
        return node.getX().accept(this) + node.getY().accept(this);
    }

}
