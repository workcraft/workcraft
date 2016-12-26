package org.workcraft.formula;

public class BooleanEvaluator implements BooleanVisitor<Boolean> {

    @Override
    public Boolean visit(And node) {
        return node.getX().accept(this) && node.getY().accept(this);
    }

    @Override
    public Boolean visit(Iff node) {
        return node.getX().accept(this) == node.getY().accept(this);
    }

    @Override
    public Boolean visit(Zero node) {
        return false;
    }

    @Override
    public Boolean visit(One node) {
        return true;
    }

    @Override
    public Boolean visit(Not node) {
        return !node.getX().accept(this);
    }

    @Override
    public Boolean visit(Imply node) {
        return !node.getX().accept(this) || node.getY().accept(this);
    }

    @Override
    public Boolean visit(BooleanVariable variable) {
        if (variable.getLabel().equals("0")) {
            return false;
        }
        if (variable.getLabel().equals("1")) {
            return true;
        }
        throw new RuntimeException("Unable to evaluate a function containing a free variable: " + variable.getLabel());
    }

    @Override
    public Boolean visit(Or node) {
        return node.getX().accept(this) || node.getY().accept(this);
    }

    @Override
    public Boolean visit(Xor node) {
        return node.getX().accept(this) ^ node.getY().accept(this);
    }
}
