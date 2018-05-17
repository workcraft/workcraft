package org.workcraft.formula.utils;

import org.workcraft.formula.And;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.formula.Iff;
import org.workcraft.formula.Imply;
import org.workcraft.formula.Not;
import org.workcraft.formula.One;
import org.workcraft.formula.Or;
import org.workcraft.formula.Xor;
import org.workcraft.formula.Zero;

public class BooleanComplementTransformer implements BooleanVisitor<BooleanFormula> {

    @Override
    public BooleanFormula visit(Zero node) {
        return One.instance();
    }

    @Override
    public BooleanFormula visit(One node) {
        return Zero.instance();
    }

    @Override
    public BooleanFormula visit(BooleanVariable node) {
        return BooleanOperations.not(node);
    }

    @Override
    public BooleanFormula visit(Not node) {
        return BooleanOperations.not(node.getX().accept(this));
    }

    @Override
    public BooleanFormula visit(And node) {
        return new Or(node.getX().accept(this), node.getY().accept(this));
    }

    @Override
    public BooleanFormula visit(Or node) {
        return new And(node.getX().accept(this), node.getY().accept(this));
    }

    @Override
    public BooleanFormula visit(Xor node) {
        return BooleanOperations.not(node);
    }

    @Override
    public BooleanFormula visit(Imply node) {
        BooleanFormula x = BooleanOperations.not(node.getX().accept(this));
        BooleanFormula y = node.getY().accept(this);
        return BooleanOperations.and(x, y);
    }

    @Override
    public BooleanFormula visit(Iff node) {
        return BooleanOperations.not(node);
    }

}
