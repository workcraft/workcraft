package org.workcraft.formula.workers;

import org.workcraft.formula.*;

public final class CleverBooleanWorker implements BooleanWorker {

    private static CleverBooleanWorker instance;

    private CleverBooleanWorker() {
    }

    public static CleverBooleanWorker getInstance() {
        if (instance == null) {
            instance = new CleverBooleanWorker();
        }
        return instance;
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
    public BooleanFormula not(BooleanFormula f) {
        return FormulaUtils.invert(f);
    }

    @Override
    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        if ((x == zero()) || (y == zero())) {
            return zero();
        }
        if (x == one()) {
            return y;
        }
        if (y == one()) {
            return x;
        }
        if (x == y) {
            return x;
        }
        return new And(x, y);
    }

    @Override
    public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
        if ((x == one()) || (y == one())) {
            return one();
        }
        if (x == zero()) {
            return y;
        }
        if (y == zero()) {
            return x;
        }
        if (x == y) {
            return x;
        }
//        String xString = StringGenerator.toString(x);
//        String yString = StringGenerator.toString(y);
//        if (xString.equals(yString) || checkStrings(xString, yString, " + ")) {
//            return x;
//        }
//        String invYText = invertString(yString);
//        if (xString.equals(invYText) || checkStrings(xString, invYText, " + ")) {
//            return one();
//        }
        return new Or(x, y);
    }

    @Override
    public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
        if (x == one()) {
            return not(y);
        }
        if (x == zero()) {
            return y;
        }
        if (y == one()) {
            return not(x);
        }
        if (y == zero()) {
            return x;
        }
        if (x == y) {
            return zero();
        }
        return new Xor(x, y);
    }

    @Override
    public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
        if ((x == zero()) || (y == one())) {
            return one();
        }
        if (x == one()) {
            return y;
        }
        if (y == zero()) {
            return not(x);
        }
        if (x == y) {
            return one();
        }
        return new Imply(x, y);
    }

    @Override
    public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        if (x == one()) {
            return y;
        }
        if (x == zero()) {
            return not(y);
        }
        if (y == one()) {
            return x;
        }
        if (y == zero()) {
            return not(x);
        }
        if (x == y) {
            return one();
        }
        return new Iff(x, y);
    }

}
