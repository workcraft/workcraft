package org.workcraft.formula.visitors;

import org.workcraft.formula.*;
import org.workcraft.formula.workers.BooleanWorker;

public class BooleanComplementTransformer implements BooleanVisitor<BooleanFormula> {

    private final BooleanWorker worker;

    public BooleanComplementTransformer(BooleanWorker worker) {
        this.worker = worker;
    }

    @Override
    public BooleanFormula visit(Zero node) {
        return worker.one();
    }

    @Override
    public BooleanFormula visit(One node) {
        return worker.zero();
    }

    @Override
    public BooleanFormula visit(BooleanVariable node) {
        return worker.not(node);
    }

    @Override
    public BooleanFormula visit(Not node) {
        return worker.not(node.getX().accept(this));
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
        return worker.not(node);
    }

    @Override
    public BooleanFormula visit(Imply node) {
        BooleanFormula x = worker.not(node.getX().accept(this));
        BooleanFormula y = node.getY().accept(this);
        return worker.and(x, y);
    }

    @Override
    public BooleanFormula visit(Iff node) {
        return worker.not(node);
    }

}
