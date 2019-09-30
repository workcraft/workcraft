package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

import java.util.LinkedList;
import java.util.List;

public class LiteralsExtractor implements BooleanVisitor<List<BooleanVariable>> {

    @Override
    public List<BooleanVariable> visit(Zero node) {
        return new LinkedList<>();
    }

    @Override
    public List<BooleanVariable> visit(One node) {
        return new LinkedList<>();
    }

    @Override
    public List<BooleanVariable> visit(BooleanVariable node) {
        List<BooleanVariable> result = new LinkedList<>();
        result.add(node);
        return result;
    }

    @Override
    public List<BooleanVariable> visit(Not node) {
        return node.getX().accept(this);
    }

    @Override
    public List<BooleanVariable> visit(And node) {
        return accept(node);
    }

    @Override
    public List<BooleanVariable> visit(Or node) {
        return accept(node);
    }

    @Override
    public List<BooleanVariable> visit(Iff node) {
        return accept(node);
    }

    @Override
    public List<BooleanVariable> visit(Xor node) {
        return accept(node);
    }

    @Override
    public List<BooleanVariable> visit(Imply node) {
        return accept(node);
    }

    private List<BooleanVariable> accept(BinaryBooleanFormula node) {
        List<BooleanVariable> result = new LinkedList<>();
        result.addAll(node.getX().accept(this));
        result.addAll(node.getY().accept(this));
        return result;
    }

}
