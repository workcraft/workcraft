package org.workcraft.formula;

public class PrettifyBooleanWorker implements BooleanWorker {

    private final ReducedBooleanWorker worker;

    public PrettifyBooleanWorker(ReducedBooleanWorker worker) {
        this.worker = worker;
    }

    @Override
    public BooleanFormula zero() {
        return Zero.instance();
    }

    @Override
    public BooleanFormula one() {
        return One.instance();
    }

    @Override
    public BooleanFormula not(BooleanFormula x) {
        if (x == Zero.instance()) {
            return One.instance();
        }
        if (x == One.instance()) {
            return Zero.instance();
        }
        return worker.not(x);
    }

    @Override
    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        if (x == y) {
            return x;
        }
        if ((x == Zero.instance()) || (y == Zero.instance())) {
            return Zero.instance();
        }
        if (x == One.instance()) {
            return y;
        }
        if (y == One.instance()) {
            return x;
        }
        return worker.and(x, y);
    }

    @Override
    public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
        return not(and(not(x), not(y)));
    }

    @Override
    public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
        return not(iff(x, y));
    }

    @Override
    public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
        return or(not(x), y);
    }

    @Override
    public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        if (x == y) {
            return One.instance();
        }
        if (x == Zero.instance()) {
            return not(y);
        }
        if (x == One.instance()) {
            return y;
        }
        if (y == Zero.instance()) {
            return not(x);
        }
        if (y == One.instance()) {
            return x;
        }
        return worker.iff(x, y);
    }

}
