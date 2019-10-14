package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

public final class Inverter implements BooleanVisitor<BooleanFormula> {

    private static Inverter instance;

    private Inverter() {
    }

    public static Inverter getInstance() {
        if (instance == null) {
            instance = new Inverter();
        }
        return instance;
    }

    @Override
    public BooleanFormula visit(Not node) {
        return node.getX();
    }

    @Override
    public BooleanFormula visit(And node) {
        return new Not(node);
    }

    @Override
    public BooleanFormula visit(Iff node) {
        return new Not(node);
    }

    @Override
    public BooleanFormula visit(Xor node) {
        return new Not(node);
    }

    @Override
    public BooleanFormula visit(Zero node) {
        return One.getInstance();
    }

    @Override
    public BooleanFormula visit(One node) {
        return Zero.getInstance();
    }

    @Override
    public BooleanFormula visit(Imply node) {
        return new Not(node);
    }

    @Override
    public BooleanFormula visit(BooleanVariable node) {
        return new Not(node);
    }

    @Override
    public BooleanFormula visit(Or node) {
        return new Not(node);
    }
}
