package org.workcraft.formula;

public class RecursiveBooleanVisitor<T> implements BooleanVisitor<T> {
    protected T visitDefault(BooleanFormula node) {
        return null;
    }

    protected T visitBinaryAfterSubnodes(BinaryBooleanFormula node, T x, T y) {
        return visitDefault(node);
    }

    protected T visitBinary(BinaryBooleanFormula node) {
        return visitBinaryAfterSubnodes(node, node.getX().accept(this), node.getY().accept(this));
    }

    @Override
    public T visit(And node) {
        return visitBinary(node);
    }

    @Override
    public T visit(Iff node) {
        return visitBinary(node);
    }

    @Override
    public T visit(Zero node) {
        return visitDefault(node);
    }

    @Override
    public T visit(One node) {
        return visitDefault(node);
    }

    @Override
    public T visit(Not node) {
        node.getX().accept(this);
        return visitDefault(node);
    }

    @Override
    public T visit(Imply node) {
        return visitBinary(node);
    }

    @Override
    public T visit(BooleanVariable node) {
        return visitDefault(node);
    }

    @Override
    public T visit(Or node) {
        return visitBinary(node);
    }

    @Override
    public T visit(Xor node) {
        return visitBinary(node);
    }
}
