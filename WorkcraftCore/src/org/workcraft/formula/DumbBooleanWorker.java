package org.workcraft.formula;

public class DumbBooleanWorker implements BooleanWorker {

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
        return new Not(x);
    }

    @Override
    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        return new And(x, y);
    }

    @Override
    public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
        return new Or(x, y);
    }

    @Override
    public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
        return not(new Iff(x, y));
    }

    @Override
    public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
        return new Imply(x, y);
    }

    @Override
    public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        return new Iff(x, y);
    }

}
