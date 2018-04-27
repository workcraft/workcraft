package org.workcraft.formula;

import org.workcraft.formula.utils.FormulaToString;

public class CleverBooleanWorker implements BooleanWorker {

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
        if (x == One.instance()) {
            return Zero.instance();
        }
        if (x == Zero.instance()) {
            return One.instance();
        }
        return new Not(x);
    }

    @Override
    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        if (FormulaToString.toString(x).equals(FormulaToString.toString(y))) {
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
        return new And(x, y);
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
            return One.instance();
        }
        if ((x == One.instance()) || (y == One.instance())) {
            return One.instance();
        }
        if (x == Zero.instance()) {
            return y;
        }
        if (y == Zero.instance()) {
            return x;
        }
        return new Or(x, y);
    }

    @Override
    public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
        if (FormulaToString.toString(x).equals(FormulaToString.toString(y))) {
            return Zero.instance();
        }
        if (x == One.instance()) {
            return not(y);
        }
        if (x == Zero.instance()) {
            return y;
        }
        if (y == One.instance()) {
            return not(x);
        }
        if (y == Zero.instance()) {
            return x;
        }
        return new Xor(x, y);
    }

    @Override
    public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
        if (FormulaToString.toString(x).equals(FormulaToString.toString(y))) {
            return One.instance();
        }
        if ((x == Zero.instance()) || (y == One.instance())) {
            return One.instance();
        }
        if (x == One.instance()) {
            return y;
        }
        if (y == Zero.instance()) {
            return not(x);
        }
        return new Imply(x, y);
    }

    @Override
    public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        if (FormulaToString.toString(x).equals(FormulaToString.toString(y))) {
            return One.instance();
        }
        if (x == One.instance()) {
            return y;
        }
        if (x == Zero.instance()) {
            return not(y);
        }
        if (y == One.instance()) {
            return x;
        }
        if (y == Zero.instance()) {
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
