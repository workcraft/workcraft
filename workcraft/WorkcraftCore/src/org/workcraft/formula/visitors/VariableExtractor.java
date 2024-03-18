package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VariableExtractor implements BooleanVisitor<Set<BooleanVariable>> {

    @Override
    public Set<BooleanVariable> visit(Zero node) {
        return Collections.emptySet();
    }

    @Override
    public Set<BooleanVariable> visit(One node) {
        return Collections.emptySet();
    }

    @Override
    public Set<BooleanVariable> visit(BooleanVariable node) {
        return Collections.singleton(node);
    }

    @Override
    public Set<BooleanVariable> visit(Not node) {
        return node.getX().accept(this);
    }

    @Override
    public Set<BooleanVariable> visit(And node) {
        return visitBinaryOperator(node);
    }

    @Override
    public Set<BooleanVariable> visit(Or node) {
        return visitBinaryOperator(node);
    }

    @Override
    public Set<BooleanVariable> visit(Iff node) {
        return visitBinaryOperator(node);
    }

    @Override
    public Set<BooleanVariable> visit(Xor node) {
        return visitBinaryOperator(node);
    }

    @Override
    public Set<BooleanVariable> visit(Imply node) {
        return visitBinaryOperator(node);
    }

    private Set<BooleanVariable> visitBinaryOperator(BinaryBooleanFormula node) {
        Set<BooleanVariable> result = new HashSet<>();
        result.addAll(node.getX().accept(this));
        result.addAll(node.getY().accept(this));
        return result;
    }

}
