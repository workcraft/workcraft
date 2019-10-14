package org.workcraft.formula.workers;

import org.workcraft.formula.*;
import org.workcraft.formula.visitors.StringGenerator;

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
    public BooleanFormula not(BooleanFormula x) {
        return FormulaUtils.invert(x);
    }

    @Override
    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        if (StringGenerator.toString(x).equals(StringGenerator.toString(y))) {
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
        return new And(x, y);
    }

    @Override
    public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
        if (StringGenerator.toString(x).equals(StringGenerator.toString(y))) {
            return x;
        }
        if (checkStrings(StringGenerator.toString(x), StringGenerator.toString(y), " + ")) {
            return x;
        }
        if (checkStrings(StringGenerator.toString(x), invertString(StringGenerator.toString(y)), " + ")) {
            return One.getInstance();
        }
        if ((x == One.getInstance()) || (y == One.getInstance())) {
            return One.getInstance();
        }
        if (x == Zero.getInstance()) {
            return y;
        }
        if (y == Zero.getInstance()) {
            return x;
        }
        return new Or(x, y);
    }

    @Override
    public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
        if (StringGenerator.toString(x).equals(StringGenerator.toString(y))) {
            return Zero.getInstance();
        }
        if (x == One.getInstance()) {
            return not(y);
        }
        if (x == Zero.getInstance()) {
            return y;
        }
        if (y == One.getInstance()) {
            return not(x);
        }
        if (y == Zero.getInstance()) {
            return x;
        }
        return new Xor(x, y);
    }

    @Override
    public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
        if (StringGenerator.toString(x).equals(StringGenerator.toString(y))) {
            return One.getInstance();
        }
        if ((x == Zero.getInstance()) || (y == One.getInstance())) {
            return One.getInstance();
        }
        if (x == One.getInstance()) {
            return y;
        }
        if (y == Zero.getInstance()) {
            return not(x);
        }
        return new Imply(x, y);
    }

    @Override
    public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        if (StringGenerator.toString(x).equals(StringGenerator.toString(y))) {
            return One.getInstance();
        }
        if (x == One.getInstance()) {
            return y;
        }
        if (x == Zero.getInstance()) {
            return not(y);
        }
        if (y == One.getInstance()) {
            return x;
        }
        if (y == Zero.getInstance()) {
            return not(x);
        }
        return new Iff(x, y);
    }

    private boolean checkStrings(String x, String y, String op) {
        if (x.contains(y)) {
            if (x.startsWith(y + op)) {
                return true;
            } else if (x.endsWith(op + y)) {
                return true;
            } else if (x.contains(op + y + op)) {
                return true;
            }
        }
        return false;
    }

    private String invertString(String x) {
        String result = "";
        //find first operator
        int op = x.indexOf("*");
        if ((x.indexOf(" + ") < op) && (x.indexOf(" + ") > 0)) {
            op = x.indexOf(" + ");
            if (x.substring(0, op).contains("'")) {
                result = x.substring(0, op).replace("'", "");
            } else {
                result = x.substring(0, op) + "'";
            }
            result = result + x.substring(op, op + 2) + invertString(x.substring(op + 2));
            return result;
        }
        if (op == -1) {
            if (x.contains("'")) {
                return x.replace("'", "");
            } else {
                return x + "'";
            }
        }
        if (x.substring(0, op).contains("'")) {
            result = x.substring(0, op).replace("'", "");
        } else {
            result = x.substring(0, op) + "'";
        }
        result = result + x.charAt(op) + invertString(x.substring(op + 1));
        return result;
    }

}
