package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

import java.util.LinkedHashSet;

public class OrderedVariableExtractor implements BooleanVisitor<LinkedHashSet<BooleanVariable>> {

    private final LinkedHashSet<BooleanVariable> variables = new LinkedHashSet<>();

    @Override
    public LinkedHashSet<BooleanVariable> visit(Zero node) {
        return variables;
    }

    @Override
    public LinkedHashSet<BooleanVariable> visit(One node) {
        return variables;
    }

    @Override
    public LinkedHashSet<BooleanVariable> visit(BooleanVariable node) {
        variables.add(node);
        return variables;
    }

    @Override
    public LinkedHashSet<BooleanVariable> visit(Not node) {
        return node.getX().accept(this);
    }

    @Override
    public LinkedHashSet<BooleanVariable> visit(And node) {
        return visitBinaryOperator(node);
    }

    @Override
    public LinkedHashSet<BooleanVariable> visit(Or node) {
        return visitBinaryOperator(node);
    }

    @Override
    public LinkedHashSet<BooleanVariable> visit(Iff node) {
        return visitBinaryOperator(node);
    }

    @Override
    public LinkedHashSet<BooleanVariable> visit(Xor node) {
        return visitBinaryOperator(node);
    }

    @Override
    public LinkedHashSet<BooleanVariable> visit(Imply node) {
        return visitBinaryOperator(node);
    }

    private LinkedHashSet<BooleanVariable> visitBinaryOperator(BinaryBooleanFormula node) {
        variables.addAll(node.getX().accept(this));
        variables.addAll(node.getY().accept(this));
        return variables;
    }

}
