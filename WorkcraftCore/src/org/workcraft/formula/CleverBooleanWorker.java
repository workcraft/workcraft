package org.workcraft.formula;

import org.workcraft.formula.utils.FormulaToString;

public class CleverBooleanWorker implements BooleanWorker {
    private static final BooleanFormula ZERO = Zero.instance();
    private static final BooleanFormula ONE = One.instance();

    @Override
    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        if (FormulaToString.toString(x).equals(FormulaToString.toString(y))) {
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
        return new And(x, y);
    }

    @Override
    public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        if (FormulaToString.toString(x).equals(FormulaToString.toString(y))) {
            return ONE;
        }
        if (x == ONE) {
            return y;
        }
        if (x == ZERO) {
            return not(y);
        }
        if (y == ONE) {
            return x;
        }
        if (y == ZERO) {
            return not(x);
        }
        return new Iff(x, y);
    }

    @Override
    public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
        if (FormulaToString.toString(x).equals(FormulaToString.toString(y))) {
            return ONE;
        }
        if (x == ZERO || y == ONE) {
            return ONE;
        }
        if (x == ONE) {
            return y;
        }
        if (y == ZERO) {
            return not(x);
        }
        return new Imply(x, y);
    }

    @Override
    public BooleanFormula not(BooleanFormula x) {
        if (x == ONE) {
            return ZERO;
        }
        if (x == ZERO) {
            return ONE;
        }
        return new Not(x);
    }

    @Override
    public BooleanFormula one() {
        return One.instance();
    }

    @Override
    public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
        if (FormulaToString.toString(x).equals(FormulaToString.toString(y))) {
            return x;
        }
        if (checkStrings(FormulaToString.toString(x), FormulaToString.toString(y), " + ")) {
            return x;
        }
        if (checkStrings(FormulaToString.toString(x), invertString(FormulaToString.toString(y)), " + ")) {
            return ONE;
        }
        if (x == ONE || y == ONE) {
            return ONE;
        }
        if (x == ZERO) {
            return y;
        }
        if (y == ZERO) {
            return x;
        }
        return new Or(x, y);
    }

    @Override
    public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
        if (FormulaToString.toString(x).equals(FormulaToString.toString(y))) {
            return ZERO;
        }
        if (x == ONE) {
            return not(y);
        }
        if (x == ZERO) {
            return y;
        }
        if (y == ONE) {
            return not(x);
        }
        if (y == ZERO) {
            return x;
        }
        return new Xor(x, y);
    }

    @Override
    public BooleanFormula zero() {
        return Zero.instance();
    }

    public boolean checkStrings(String x, String y, String op) {

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

    public String invertString(String x) {
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
