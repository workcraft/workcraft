package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

import java.util.ArrayList;
import java.util.List;

public class OrderedVariableExtractor implements BooleanVisitor<List<BooleanVariable>> {

    private final List<BooleanVariable> variables = new ArrayList<>();

    @Override
    public List<BooleanVariable> visit(Zero node) {
        return variables;
    }

    @Override
    public List<BooleanVariable> visit(One node) {
        return variables;
    }

    @Override
    public List<BooleanVariable> visit(BooleanVariable node) {
        variables.add(node);
        return variables;
    }

    @Override
    public List<BooleanVariable> visit(Not node) {
        return node.getX().accept(this);
    }

    @Override
    public List<BooleanVariable> visit(And node) {
        return visitBinaryOperator(node);
    }

    @Override
    public List<BooleanVariable> visit(Or node) {
        return visitBinaryOperator(node);
    }

    @Override
    public List<BooleanVariable> visit(Iff node) {
        return visitBinaryOperator(node);
    }

    @Override
    public List<BooleanVariable> visit(Xor node) {
        return visitBinaryOperator(node);
    }

    @Override
    public List<BooleanVariable> visit(Imply node) {
        return visitBinaryOperator(node);
    }

    private List<BooleanVariable> visitBinaryOperator(BinaryBooleanFormula node) {
        node.getX().accept(this);
        node.getY().accept(this);
        return variables;
    }

}
