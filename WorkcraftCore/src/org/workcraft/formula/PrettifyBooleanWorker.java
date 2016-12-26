package org.workcraft.formula;

public class PrettifyBooleanWorker implements BooleanWorker {
    private final ReducedBooleanWorker worker;
    private static final Zero ZERO = Zero.instance();
    private static final One ONE = One.instance();

    public PrettifyBooleanWorker(ReducedBooleanWorker worker) {
        this.worker = worker;
    }

    public BooleanFormula not(BooleanFormula x) {
        if (x == ZERO) {
            return ONE;
        }
        if (x == ONE) {
            return ZERO;
        }
        return worker.not(x);
    }

    public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        if (x == y) {
            return ONE;
        }
        if (x == ZERO) {
            return not(y);
        }
        if (x == ONE) {
            return y;
        }
        if (y == ZERO) {
            return not(x);
        }
        if (y == ONE) {
            return x;
        }
        return worker.iff(x, y);
    }

    public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
        return or(not(x), y);
    }

    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        if (x == y) {
            return x;
        }
        if (x == ZERO || y == ZERO) {
            return ZERO;
        }
        if (x == ONE) {
            return y;
        }
        if (y == ONE) {
            return x;
        }
        return worker.and(x, y);
    }

    public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
        return not(and(not(x), not(y)));
    }

    @Override
    public BooleanFormula one() {
        return ONE;
    }

    @Override
    public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
        return not(iff(x, y));
    }

    @Override
    public BooleanFormula zero() {
        return ZERO;
    }
}
