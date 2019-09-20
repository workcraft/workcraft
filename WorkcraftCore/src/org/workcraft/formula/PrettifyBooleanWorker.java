package org.workcraft.formula;

public class PrettifyBooleanWorker implements BooleanWorker {

    private final ReducedBooleanWorker worker;

    public PrettifyBooleanWorker(ReducedBooleanWorker worker) {
        this.worker = worker;
    }

    @Override
    public BooleanFormula zero() {
        return Zero.getInstance();
    }

    @Override
    public BooleanFormula one() {
        return One.getInstance();
    }

    @Override
    public BooleanFormula not(BooleanFormula x) {
        if (x == Zero.getInstance()) {
            return One.getInstance();
        }
        if (x == One.getInstance()) {
            return Zero.getInstance();
        }
        return worker.not(x);
    }

    @Override
    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        if (x == y) {
            return x;
        }
        if ((x == Zero.getInstance()) || (y == Zero.getInstance())) {
            return Zero.getInstance();
        }
        if (x == One.getInstance()) {
            return y;
        }
        if (y == One.getInstance()) {
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
            return One.getInstance();
        }
        if (x == Zero.getInstance()) {
            return not(y);
        }
        if (x == One.getInstance()) {
            return y;
        }
        if (y == Zero.getInstance()) {
            return not(x);
        }
        if (y == One.getInstance()) {
            return x;
        }
        return worker.iff(x, y);
    }

}
