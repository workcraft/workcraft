package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

import java.util.HashSet;
import java.util.Set;

public class BubbledLiteralsExtractor implements BooleanVisitor<Set<BooleanVariable>> {

    private boolean negation = false;

    @Override
    public Set<BooleanVariable> visit(Zero node) {
        return new HashSet<>();
    }

    @Override
    public Set<BooleanVariable> visit(One node) {
        return new HashSet<>();
    }

    @Override
    public Set<BooleanVariable> visit(BooleanVariable node) {
        Set<BooleanVariable> result = new HashSet<>();
        if (negation) {
            result.add(node);
            negation = false;
        }
        return result;
    }

    @Override
    public Set<BooleanVariable> visit(Not node) {
        negation = true;
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
        negation = false;
        Set<BooleanVariable> result = new HashSet<>();
        result.addAll(node.getX().accept(this));
        result.addAll(node.getY().accept(this));
        return result;
    }

}
