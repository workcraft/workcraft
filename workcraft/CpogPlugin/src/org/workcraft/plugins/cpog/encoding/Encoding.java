package org.workcraft.plugins.cpog.encoding;

import org.workcraft.formula.BooleanFormula;

public class Encoding {

    private BooleanFormula[] formulas;
    private boolean[][] code;

    public Encoding(BooleanFormula[] functions, boolean[][] code) {
        this.formulas = functions;
        this.code = code;
    }

    public BooleanFormula[] getFormulas() {
        return formulas;
    }

    public boolean[][] getCode() {
        return code;
    }

    public void setCode(boolean[][] code) {
        this.code = code;
    }

    public void setFormula(BooleanFormula formula, int index) {
        this.formulas[index] = formula;
    }

    public void setFormulas(BooleanFormula[] formulas) {
        this.formulas = formulas;
    }

}
