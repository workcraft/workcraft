package org.workcraft.formula.encoding;

import org.workcraft.formula.BooleanFormula;

public class Encoding {
    public Encoding(boolean[][] encoding, BooleanFormula[] functions) {
        this.encoding = encoding;
        this.functions = functions;
    }
    public BooleanFormula[] getFunctions() {
        return functions;
    }
    public boolean[][] getEncoding() {
        return encoding;
    }
    public void setEncoding(boolean[][] encoding) {
        this.encoding = encoding;
    }
    public void setFormula(BooleanFormula formula, int index) {
        this.functions[index] = formula;
    }
    public void setFormule(BooleanFormula[] formula) {
        this.functions = formula;
    }
    private  BooleanFormula[] functions;
    private boolean[][] encoding;
}
