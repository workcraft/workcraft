package org.workcraft.formula.utils;

import org.workcraft.formula.And;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.formula.Iff;
import org.workcraft.formula.Imply;
import org.workcraft.formula.Not;
import org.workcraft.formula.One;
import org.workcraft.formula.Or;
import org.workcraft.formula.Xor;
import org.workcraft.formula.Zero;

public class FormulaToLiteralCount implements BooleanVisitor<Integer> {

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
        return node.getX().accept(this) + node.getY().accept(this);
    }

    @Override
    public Integer visit(Or node) {
        return node.getX().accept(this) + node.getY().accept(this);
    }

    @Override
    public Integer visit(Iff node) {
        return node.getX().accept(this) + node.getY().accept(this);
    }

    @Override
    public Integer visit(Xor node) {
        return node.getX().accept(this) + node.getY().accept(this);
    }

    @Override
    public Integer visit(Imply node) {
        return node.getX().accept(this) + node.getY().accept(this);
    }

}
